const { skyonicsUsers } = require("../data/users");

const fs = require("fs");

class SkyonicsSession {
  constructor({
    url = "https://www.skyonics.net/api/",
    username,
    password,
  } = {}) {
    this.url = url;
    this.username = username;
    this.password = password;
    this.headers = {
      Accept: "application/json, text/plain, */*",
    };
    this.customerNumber = null;
    this.customerName = null;
  }

  async get(path) {
    try {
      const response = await fetch(`${this.url}${path}`, {
        method: "GET",
        headers: this.headers,
      });
      if (response.ok) {
        return response;
      } else {
        console.error("GET request failed:", response.statusText);
        return null;
      }
    } catch (error) {
      console.error("GET request error:", error);
    }
  }
  async post(path, body) {
    try {
      const response = await fetch(`${this.url}${path}`, {
        method: "POST",
        headers: this.headers,
        body: body,
      });

      if (response.ok) {
        return response;
      } else {
        console.error("POST request failed:", response.statusText);
        return null;
      }
    } catch (error) {
      console.error("POST request error:", error);
    }
  }

  async open() {
    try {
      const urlEncodedParams = new URLSearchParams();
      urlEncodedParams.append("UserName", this.username);
      urlEncodedParams.append("Password", this.password);

      this.headers["Content-Type"] = "application/x-www-form-urlencoded";

      const response = await this.post(
        "proxy/userlogin/login",
        urlEncodedParams,
      );
      let cookies = [];
      for (const [key, value] of response.headers.entries()) {
        if (key.toLowerCase() === "set-cookie") {
          cookies.push(value.split(";")[0]);
        }
      }
      this.headers["Cookie"] = cookies.join("; ");
      console.log("Login uspesan za usera: " + this.username);

      const customerResponse = await this.get(
        `proxy/devicehealthoperations/getcustomers`,
      );

      const customerData = await customerResponse.json();
      this.customerNumber = customerData.Data.Data[0].CustomerNumber;
    } catch (error) {
      console.error("POST request error:", error);
    }
  }

  async logout() {
    try {
      const response = await fetch(
        `https://www.skyonics.net/api/proxy/userlogin/logout`,
        {
          method: "PUT",
          headers: this.headers,
        },
      );
      if (response.ok) {
        console.log("Logout uspesan za usera: " + this.username);
      } else {
        console.error("PUT request failed:", response.statusText);
      }
    } catch (error) {
      console.error("PUT request error:", error);
    }
  }

  async getDeviceSn({ page = 0 } = {}) {
    let eldSNs = [];
    while (true) {
      const urlParams = {
        page: page,
        customer: this.customerNumber,
        healthProblemExplanation: "",
        serialNumberFilter: "",
        showSuppressed: false,
        sortBy: "",
        lastData: "",
        noVIN: false,
        version: "",
        custom: "",
        showOnlyBookmarked: false,
      };

      const urlEncodedParams = new URLSearchParams(urlParams).toString();
      console.log(this.headers);
      try {
        const response = await this.get(
          `proxy/devicehealthoperations/getdeviceshealthinfo?${urlEncodedParams}`,
        );

        const data = await response.json();
        if (!data.Data.Data || data.Data.Data.length < 1) break;

        for (const device of data.Data.Data) {
          eldSNs.push(device.DeviceSerialNumber);
        }
        page++;
        break;
      } catch (error) {
        console.error("Greska pri citanju uredjaja:", error);
        break;
      }
    }
    return eldSNs;
  }

  async getDeviceHealth(sn) {
    try {
      const urlParams = {
        page: 0,
        last: true,
        serialNumber: sn,
        stDate: "2026-11-20T06:00:00.000Z",
        endDate: "2025-11-27T03:59:59.999Z",
        tzoffset: -360,
      };

      const urlEncodedParams = new URLSearchParams(urlParams).toString();

      const response = await this.get(
        `proxy/devicehealthoperations/getdevicearchive?${urlEncodedParams}`,
      );
      const data = await response.json();

      if (!data.Data.Data || data.Data.Data.length < 1) {
        console.log("Nije pronadjen uredjaj. SN: " + sn);
        return null;
      }
      const packets = data.Data.Data;
      for (let i = 0; i < 10; i++) {
        if (packets[i].DataPacket.startsWith("6A02")) {
          console.log(`Dobar uredjaj: ${packets[i].SensorId}`);
          return null;
        }
        if (
          packets[i].DataPacket.startsWith("0266") ||
          packets[i].DataPacket.startsWith("48D0") ||
          packets[i].DataPacket.startsWith("8FCA") ||
          packets[i].DataPacket.startsWith("820F") ||
          packets[i].DataPacket.startsWith("F001")
        ) {
          return packets[i].SensorId;
          console.log(`Upisan ${packets[i].SensorId}`);
        }
      }
      return null;
    } catch (err) {
      console.error("Error fetching device health info:", err);
      return null;
    }
  }
  async getLastPackets(sn, pagenumberOfPackets = 5) {
    const beginDate = new Date(Date.now() - 1 * 86400000).toISOString();
    const endDate = new Date().toISOString();
    try {
      const urlParams = {
        Orign: "siteOperationsManagerDeviceanalysisPackets",
        GraphColumn: "OBDOdometer",
        SerialNumber: sn,
        Begin: beginDate,
        End: endDate,
        TZOffset: -360,
      };

      const urlEncodedParams = new URLSearchParams(urlParams).toString();

      const response = await this.post(
        `proxy/devicehealthoperations/getanalysisview`,
        urlEncodedParams,
      );
      const data = await response.json();
      const tableData = data.Data.TableData.filter((row) => row.RowData.MessageReason);


      if (tableData.length < 1) return null;

      const lastPacket = tableData[0];
      let IGN_OFF =
        lastPacket.RowData.MessageReason == "POWER_CUT" ||
        lastPacket.RowData.MessageReason == "POWER_OFF" ||
        lastPacket.RowData.MessageReason == "IGN_OFF"
          ? true
          : false;

      if (IGN_OFF == true)
        return { deviceSerialNumber: sn, message: "Iskljucen kamion" };

      const bleData = {
        deviceSerialNumber: sn,
        BLE: false,
        firmwareVer55: null,
      };
      const lastPackets = tableData
        .filter((row) => row.RowData.MessageReason == "ON_PERIODIC")
        .slice(0, pagenumberOfPackets)
        .reverse();

      lastPackets.forEach((packet) => {
        if (packet.RowData.BLEClient) {
          bleData.BLE = true;
        } else {
          bleData.BLE = false;
        }
        if (packet.RowData.BluetoothFirmwareVersion) {
          {
            if (
              packet.RowData.BluetoothFirmwareVersion == "55" ||
              packet.RowData.BluetoothFirmwareVersion == "66"
            ) {
              bleData.firmwareVer55 = true;
            } else {
              bleData.firmwareVer55 = false;
            }
          }
        }
      });

      return bleData;
    } catch (err) {
      console.error("Error fetching device health info:", err);
      return null;
    }
  }

  async getCustomerName(sn) {
    try {
      const response = await this.get(
        `proxy/devicehealthoperations/getdevicehealthinfo?deviceSerialNumber=${sn}`,
      );
      const data = await response.json();
      const customerName = data.Data.CustomerName;
      this.customerName = customerName;
      return customerName;
    } catch (err) {
      console.error("Error fetching device health info:", err);
      return null;
    }
  }

  async getOBDdata(sn) {
    try {
      const urlParams = {
        Orign: "siteOperationsManagerDeviceanalysisPackets",
        GraphColumn: "OBDOdometer",
        SerialNumber: sn,
        Begin: "2026-02-24T06:00:00.000Z",
        End: "2026-03-03T23:59:59.999Z",
        TZOffset: -360,
      };

      const urlEncodedParams = new URLSearchParams(urlParams).toString();

      const response = await this.post(
        `proxy/devicehealthoperations/getanalysisview`,
        urlEncodedParams,
      );
      const data = await response.json();
      const tableData = data.Data.TableData;
      if (tableData.length < 1) return null;

      const eldData = { SN: sn };
      let counter = 1;
      for (const packet of tableData.reverse()) {
        if (packet.RowData.MessageReason == "ON_PERIODIC") {
          //counter++;
          if (packet.RowData.OBDFuel) {
            eldData.OBDFuel = packet.RowData.OBDFuel;
            eldData.EventDateTimeUTC = packet.RowData.EventDateTime;
          }
          if (packet.RowData.OBDOdometer) {
            eldData.OBDOdometer = packet.RowData.OBDOdometer;
            eldData.EventDateTimeUTC = packet.RowData.EventDateTime;
          }
          if (packet.RowData.J1939EngineHours) {
            eldData.J1939EngineHours = packet.RowData.J1939EngineHours;
            eldData.EventDateTimeUTC = packet.RowData.EventDateTime;
          }
          if (packet.RowData.OBDSpeed && !eldData.OBDSpeed) {
            eldData.OBDSpeed = packet.RowData.OBDSpeed;
            eldData.EventDateTimeUTC = packet.RowData.EventDateTime;
          }

          if (packet.RowData.OBDVIN && !eldData.OBDVIN) {
            eldData.OBDVIN = packet.RowData.OBDVIN;
            eldData.EventDateTimeUTC = packet.RowData.EventDateTime;
          }
          //if (counter > 1000) break;
        }
      }
      return eldData;
    } catch (err) {
      console.error("Error fetching device analysis view:", err);
      return null;
    }
  }
}

async function decodeVins(list, elds) {
  data = [];
  let nonDecodedElds = [];
  for (const truck of list) {
    if (!truck.OBDVIN) {
      nonDecodedElds.push(truck);
      continue;
    }
    let tempData = truck;
    const response = await fetch(
      `https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/${truck.OBDVIN}?format=json`,
    );

    const responseData = await response.json();

    const make = responseData.Results[0].Make;
    const engineModel = responseData.Results[0].EngineModel;
    const engineManufacturer = responseData.Results[0].EngineManufacturer;
    const modelYear = responseData.Results[0].ModelYear;
    tempData.Make = make;
    tempData.Model_Year = modelYear;
    tempData.Engine_Manufacturer = engineManufacturer;
    tempData.Engine_Model = engineModel;
    console.log(`Processed: ${tempData.OBDVIN}`);

    data.push(tempData);
  }

  const eldsWithData = data.filter(
    (truck) =>
      truck.OBDOdometer &&
      truck.OBDVIN &&
      truck.J1939EngineHours &&
      truck.OBDFuel &&
      truck.OBDSpeed,
  );

  let eldsWithoutData = data.filter(
    (truck) =>
      !truck.OBDOdometer ||
      !truck.J1939EngineHours ||
      !truck.OBDFuel ||
      !truck.OBDSpeed,
  );

  eldsWithoutData = eldsWithoutData.concat(nonDecodedElds);

  fs.writeFileSync(
    `./Decoded${elds}withFullData.json`,
    JSON.stringify(eldsWithData, null, 2),
    "utf8",
  );

  fs.writeFileSync(
    `./Decoded${elds}WithMissingOBDData.json`,
    JSON.stringify(eldsWithoutData, null, 2),
    "utf8",
  );
}

async function getOBDdata({ filename, skyonicsUsersAcc, eldJson } = {}) {
  if (!fs.existsSync(filename)) {
    fs.writeFileSync(filename, "[]", "utf8");
  }

  for (const user of skyonicsUsersAcc) {
    const session = new SkyonicsSession({
      username: skyonicsUsers.get(user).username,
      password: skyonicsUsers.get(user).password,
    });

    const elds = JSON.parse(fs.readFileSync(`./json/${eldJson}.json`, "utf8"));
    let readElds = JSON.parse(fs.readFileSync(filename, "utf8"));

    await session.open();

    for (const eld of elds) {
      if (readElds.some((d) => d.SN === eld)) continue;

      const data = await session.getOBDdata(eld);

      if (data && !readElds.some((d) => d.SN === data.SN)) {
        readElds.push(data);

        fs.writeFileSync(filename, JSON.stringify(readElds, null, 2), "utf8");
      } else {
        console.log("Nije pronadjen uredjaj. SN:", eld);
      }
    }

    await session.logout();
  }
}

async function filterELDS() {
  const elds = JSON.parse(fs.readFileSync("./json/elds.json", "utf8"));
  const prefixes = ["88A", "88U", "88X", "88B"];

  for (const prefix of prefixes) {
    let filteredELDS = elds.filter((eld) => eld.serialNum.startsWith(prefix));
    const unique = new Set(filteredELDS.map((eld) => eld.serialNum));
    filteredELDS = Array.from(unique);
    fs.writeFileSync(
      `./json/${prefix}.json`,
      JSON.stringify(filteredELDS, null, 2),
      "utf8",
    );
  }
}

async function markBadELDS() {
  const files = fs.readdirSync("./");

  for (const file of files) {
    if (file.endsWith(".json") && file.includes("MissingOBDData")) {
      const elds = JSON.parse(fs.readFileSync(file, "utf8"));
      const badElds = [];
      for (const eld of elds) {
        if (eld.lastStatusDate <= Date.now() - 5 * 86400000) {
          badElds.push(eld);
        }
      }
      fs.writeFileSync(
        `./badElds/${file}`,
        JSON.stringify(badElds, null, 2),
        "utf8",
      );
    }
  }
}

async function main() {
  const session = new SkyonicsSession({
    username: skyonicsUsers.get("lioneight").username,
    password: skyonicsUsers.get("lioneight").password,
  });
  await session.open();
  const customerName = await session.getCustomerName("87A041970266");
  const data = await session.getLastPackets("87A041970266", 5);
  console.log(data, customerName);

  await session.logout();
}



module.exports = { SkyonicsSession };
