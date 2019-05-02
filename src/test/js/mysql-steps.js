/* eslint-disable no-undef  */
import { Given } from 'cucumber';

function query(statement, values) {
  return new Promise(function (resolve, reject) {
    mysqlConn.query(statement, values, function (error, results, fields) {
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

  await query(createStatement);
  //   final StringBuilder statementBuffer = new StringBuilder();
  //   statementBuffer
  //       .append("create table " + identifierQuoteString + table + identifierQuoteString + " (");
  //   boolean first = true;
  //   for (Entry<String, String> entry : structure.entrySet()) {
  //     if (!first) {
  //       statementBuffer.append(", ");
  //     } else {
  //       first = false;
  //     }
  //     statementBuffer.append(identifierQuoteString + entry.getKey() + identifierQuoteString + " "
  //         + entry.getValue());
  //   }
  //   statementBuffer
  //       .append(", CONSTRAINT " + identifierQuoteString + "pk_id" + identifierQuoteString
  //           + " PRIMARY KEY (" + identifierQuoteString + "id" + identifierQuoteString + "))");
  //   LOGGER.info("TABLE: {}", statementBuffer.toString());
  //   currentJt.execute(statementBuffer.toString());
});

Given('the {string} table has the following events', async function (tableName, dataTable) {
  let stmt = `INSERT INTO \`${tableName}\` (`;
  stmt += dataTable.rawTable[0].map(k => `\`${k}\``).join();
  stmt += ') values ?';
  await query(stmt, [dataTable.rawTable.slice(1)]);
});

/*
testCase:
   { sourceLocation:
      { uri: 'src/test/resources/features/basic/db-ryvr.feature',
        line: 16 },
     pickle:
      { tags: [Array],
        name: 'Find Ryvr in Collection',
        language: 'en',
        locations: [Array],
        steps: [Array] } } }
        */

Given('a database ryvr with the following configuration', async function (dataTable) {
  const config = dataTable.rowsHash();
  // make the name unique for this scenario, to prevent conflicts with other tests
  config.name = `${config.name}-${this.scenarioId}`;
  this.client.createDataSourceRyvr(mysqlConn, config);
});
