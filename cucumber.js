
const fs = require('fs');

function generateConfig(profile) {
  const RERUN = `@cucumber-${profile}.rerun`;
  const FEATURE_GLOB = fs.existsSync(RERUN) && fs.statSync(RERUN).size !== 0 ? RERUN : 'src/test/resources/features/basic/**/*.feature --tags \'@wip\'';
  const FORMAT_OPTIONS = {
    snippetInterface: 'async-await',
    snippetSyntax: './node_modules/@windyroad/cucumber-js-throwables/lib/custom-cucumber-syntax.js',
  };
  const MODULES = '--require-module @babel/register --require-module @babel/polyfill';
  const REQUIRE_GLOB = 'src/test/js/**/*.js';
  const BASE_CONFIG = `${FEATURE_GLOB} --format-options '${JSON.stringify(FORMAT_OPTIONS)}' ${MODULES} --require ${REQUIRE_GLOB} --no-strict --format rerun:${RERUN} --fail-fast`;
  if (profile === 'systemTest') {
    return `${BASE_CONFIG} --world-parameters '${JSON.stringify({
      client: 'rest',
    })}'`;
  }
  return BASE_CONFIG;
}

module.exports = {
  default: generateConfig('default'),
  systemTest: generateConfig('systemTest'),
};
