import { PendingError, stepDefinitionWrapper } from '@windyroad/cucumber-js-throwables';
import qc from '@windyroad/quick-containers-js';
import chai from 'chai';
import {
  AfterAll, Before, BeforeAll, setDefinitionFunctionWrapper, setWorldConstructor,
} from 'cucumber';
import Docker from 'dockerode';
import mysql from 'mysql';
import ShutdownHook from 'shutdown-hook';
import waitport from 'wait-port';
import { EmbeddedRyvrClient } from '../../main/js/client/embedded-client';

const shutdownHook = new ShutdownHook();
shutdownHook.register();
shutdownHook.on('ShutdownStarted', () => console.log('it has began'));
shutdownHook.on('ComponentShutdown', e => console.log('shutting down', e.name));
shutdownHook.on('ShutdownEnded', () => console.log('it has ended'));

global.expect = chai.expect;
global.PendingError = PendingError;

const DB_IMAGE = 'mysql:5.7.26';


BeforeAll({ timeout: 60000 }, async function () {
  this.containers = {};


  const docker = new Docker();
  await qc.ensurePulled(docker, DB_IMAGE, console.log);
  this.containers.mysql = await qc.ensureStarted(docker, {
    Image: DB_IMAGE,
    Tty: false,
    ExposedPorts: {
      '3306/tcp': {},
    },
    HostConfig: {
      PortBindings: { '3306/tcp': [{ HostPort: '3306' }] },
    },
    Env: [
      'MYSQL_ROOT_PASSWORD=my-secret-pw',
    ],
    name: 'qc-mysql-test',
  }, async () => {
    await waitport({
      port: 3306,
      timeout: 60000,
    });
  });


  global.mysqlConn = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'my-secret-pw',
    // database: 'my_db',
  });
  await global.mysqlConn.connect();
});


AfterAll({ timeout: 30000 }, async function () {
  // await this.containers.mysql.stop();
});

function world({ attach, parameters }) {
  this.attach = attach;
  this.parameters = parameters;
  this.client = new EmbeddedRyvrClient();
}

setWorldConstructor(world);

setDefinitionFunctionWrapper(stepDefinitionWrapper);


Before(function (testCase) {
  this.testCase = testCase;
  this.scenarioId = `${this.testCase.sourceLocation.uri.replace(/\//g, '-')}-${this.testCase.sourceLocation.line}`;
});
