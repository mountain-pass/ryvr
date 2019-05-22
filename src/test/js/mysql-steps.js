/* eslint-disable radix */
/* eslint-disable no-await-in-loop */
/* eslint-disable no-undef  */
import { Given } from 'cucumber';
import { Ryvr } from '../../main/js/model/Ryvr';
import MySqlRyvrPage from '../../main/js/ryvrs/MySqlRyvrPage';


function query(statement, values) {
  return new Promise(function (resolve, reject) {
    mysqlTestConn.query(statement, values, function (error, results, fields) {
      if (error) {
        reject(error);
      } else {
        resolve(results, fields);
      }
    });
  });
}

Given('a database {string}', async function (dbName) {
  await query(`CREATE DATABASE IF NOT EXISTS ${dbName};`);
  await query(`USE ${dbName};`);
});

Given('it has a table {string} with the following structure', async function (tableName, dataTable) {
  const hash = dataTable.rowsHash();

  await query(`drop table if exists ${tableName} CASCADE`);

  let createStatement = `create table ${tableName} (`;
  createStatement += Object.keys(hash).map(k => `${k} ${hash[k]}`).join();
  createStatement += ')';
  this.tableName = tableName;
  this.columns = hash;
  await query(createStatement);
});

Given('the {string} table has the following events', async function (tableName, dataTable) {
  const values = dataTable.rawTable.slice(1);
  if (values.length > 0) {
    let stmt = `INSERT INTO \`${tableName}\` (`;
    stmt += dataTable.rawTable[0].map(k => `\`${k}\``).join();
    stmt += ') values ?';
    await query(stmt, [dataTable.rawTable.slice(1)]);
  }
});

Given('a database ryvr with the following configuration', async function (dataTable) {
  const config = dataTable.rowsHash();
  // make the name unique for this scenario, to prevent conflicts with other tests
  const title = this.normTitle(config.name);
  const page = new MySqlRyvrPage(mysqlTestConn, config.query, parseInt(config['underlying page size']), 1);
  const mysqlRyrv = new Ryvr(title, parseInt(config['exposed page size']), page);
  console.log('Waiting for ryvr to charge');
  await mysqlRyrv.initPromise;
  console.log('charged');


  this.exposedPageSize = parseInt(config['exposed page size']);
  console.log('PAGE SIZE', this.exposedPageSize);
  const ryvrs = await this.ryvrApp.getRyvrs();
  await ryvrs.addRyvr(title, mysqlRyrv);
});

Given('it has {int} events', { timeout: 30000 }, async function (noOfEvents) {
  const batchSize = 8192;
  const batches = Math.floor(noOfEvents / batchSize);
  for (let batch = 0; batch <= batches; batch += 1) {
    let eventsInBatch = batchSize;
    if (batch === batches) {
      eventsInBatch = noOfEvents % batchSize;
    }
    const events = [];
    for (let i = 0; i < eventsInBatch; i += 1) {
      const event = {
        id: i + batch * batchSize,
        account: '78901234',
        description: 'Buying Stuff',
        amount: i * -20.00 - (i + batch * batchSize),
        // created: moment().valueOf(),
      };
      events.push([event.id, event.account, event.description, event.amount]);
    }
    // eslint-disable-next-line no-loop-func
    await new Promise((resovle, reject) => {
      mysqlTestConn.query(`INSERT INTO ${this.tableName} (\`id\`, \`account\`, \`description\`, \`amount\`) VALUES ?`, [events], (error, results) => {
        if (error) {
          reject(error);
        } else {
          resovle(results);
        }
      });
    });
    const added = eventsInBatch + batch * batchSize;
    const percent = (added * 1.0) / noOfEvents * 100.0;
    console.log(`Added ${added} records of ${noOfEvents}. ${percent}%`);
  }
  this.expectedRecordCount = noOfEvents;
});
