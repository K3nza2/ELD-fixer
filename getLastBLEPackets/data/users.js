require("dotenv").config({ path: require("path").resolve(__dirname, "../.env") });


const castUsers = new Map();

const skyonicsUsers = new Map();

castUsers.set("lioneight", {
  username: process.env.CAST_LIONEIGHT_USER,
  password: process.env.CAST_LIONEIGHT_PASS,
});
castUsers.set("darex", {
  username: process.env.CAST_DAREX_USER,
  password: process.env.CAST_DAREX_PASS,
});
castUsers.set("optima", {
  username: process.env.CAST_OPTIMA_USER,
  password: process.env.CAST_OPTIMA_PASS,
});


skyonicsUsers.set('darex', {
  username: process.env.SKYONICS_DAREX_USER,
  password: process.env.SKYONICS_DAREX_PASS,
});

skyonicsUsers.set('optima', {
  username: process.env.SKYONICS_OPTIMA_USER,
  password: process.env.SKYONICS_OPTIMA_PASS,
});

skyonicsUsers.set('lioneight', {
  username: process.env.SKYONICS_LIONEIGHT_USER,
  password: process.env.SKYONICS_LIONEIGHT_PASS,
});

module.exports = { castUsers, skyonicsUsers };
