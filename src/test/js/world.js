import { PendingError, stepDefinitionWrapper } from '@windyroad/cucumber-js-throwables';
import qc from '@windyroad/quick-containers-js';
import chai from 'chai';
import chaiIterator from 'chai-iterator';
import {
  AfterAll, Before, BeforeAll, setDefinitionFunctionWrapper, setWorldConstructor,
} from 'cucumber';
import Docker from 'dockerode';
import fastify from 'fastify';
import mysql from 'mysql';
import ShutdownHook from 'shutdown-hook';
import { RyvrApp } from '../../main/js/model/RyvrApp';
import routes from '../../main/js/routes';
import RyvrEmbeddedDriver from './drivers/RyvrEmbeddedDriver';
import RyvrRestDriver from './drivers/RyvrRestDriver';


chai.use(chaiIterator);

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

  this.containers.mysql = await qc.ensureMySqlStarted(docker, '5.7.26');


  global.mysqlConn = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'my-secret-pw',
  });

  await new Promise(function (resolve, reject) {
    mysqlConn.connect(function (err) {
      if (err) {
        reject(err);
      } else {
        resolve(mysqlConn);
      }
    });
  });
  global.fastifyServer = fastify({
    logger: true,
  });
  global.ryvrApp = new RyvrApp();
  global.fastifyServer.register(routes, { ryvrApp: global.ryvrApp });
  global.serverUrl = await global.fastifyServer.listen(3000);
});


AfterAll({ timeout: 30000 }, async function () {
  await new Promise(function (resolve, reject) {
    mysqlConn.end(function (err) {
      if (err) {
        reject(err);
      } else {
        resolve(mysqlConn);
      }
    });
  });
  if (global.fastifyServer) {
    await global.fastifyServer.close();
  }
});


function world({ attach, parameters }) {
  this.attach = attach;
  this.parameters = parameters;
  this.ryvrApp = global.ryvrApp;
  this.ryvrApp.reset();
  switch (parameters.client) {
    case 'rest':
      this.driver = new RyvrRestDriver(global.serverUrl);
      break;
    default:
      this.driver = new RyvrEmbeddedDriver(global.ryvrApp);
      break;
  }
}

setWorldConstructor(world);

setDefinitionFunctionWrapper(stepDefinitionWrapper);


Before(function (testCase) {
  this.testCase = testCase;
  this.scenarioId = `${this.testCase.sourceLocation.uri.replace(/\//g, '-')}-${this.testCase.sourceLocation.line}`;
  this.normTitle = title => `${title}-${this.scenarioId}`;
});
