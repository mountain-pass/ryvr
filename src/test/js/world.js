import { PendingError, stepDefinitionWrapper } from '@windyroad/cucumber-js-throwables';
import chai from 'chai';
import { setDefinitionFunctionWrapper, setWorldConstructor } from 'cucumber';
import { EmbeddedRyvrClient } from '../../main/js/client/embedded-client';

global.expect = chai.expect;
global.PendingError = PendingError;

function world({ attach, parameters }) {
  this.attach = attach;
  this.parameters = parameters;
  this.client = new EmbeddedRyvrClient();
}

setWorldConstructor(world);

setDefinitionFunctionWrapper(stepDefinitionWrapper);
