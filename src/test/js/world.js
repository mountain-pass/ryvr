import { PendingError, stepDefinitionWrapper } from '@windyroad/cucumber-js-throwables';
import qc from '@windyroad/quick-containers-js';
import chai from 'chai';
import {
  AfterAll, Before, BeforeAll, setDefinitionFunctionWrapper, setWorldConstructor,
} from 'cucumber';
import Docker from 'dockerode';
import mysql from 'mysql';
import ShutdownHook from 'shutdown-hook';
import { RyvrClient } from '../../main/js/client/index';
import { RyvrApp } from '../../main/js/ryvr-app';

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
});

function world({ attach, parameters }) {
  this.attach = attach;
  this.parameters = parameters;
  this.ryvrApp = new RyvrApp();
  this.client = new RyvrClient(this.ryvrApp);
}

setWorldConstructor(world);

setDefinitionFunctionWrapper(stepDefinitionWrapper);


Before(function (testCase) {
  this.testCase = testCase;
  this.scenarioId = `${this.testCase.sourceLocation.uri.replace(/\//g, '-')}-${this.testCase.sourceLocation.line}`;
});
