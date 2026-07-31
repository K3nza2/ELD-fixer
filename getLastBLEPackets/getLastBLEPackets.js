const { SkyonicsSession } = require("./session/skyonicsSession");
const { skyonicsUsers } = require("./data/users");
const { askQuestion } = require("./helpers/askQuestion");

async function main() {
  const eldSN = await askQuestion("Unesite ELD SN: ");
  const session = new SkyonicsSession({
    username: skyonicsUsers.get("lioneight").username,
    password: skyonicsUsers.get("lioneight").password,
  });
  await session.open();
  const data = await session.getLastPackets(eldSN, 10);
  const CustomerName = await session.getCustomerName(eldSN);
  console.log({...data, CustomerName });
  await session.logout();
  return {...data, CustomerName }
}

main();
