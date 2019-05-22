/* eslint-disable no-await-in-loop */
/* eslint-disable no-restricted-syntax */
import { PendingError } from '@windyroad/cucumber-js-throwables/lib/pending-error';
import { Given, Then, When } from 'cucumber';

const { Stats } = require('fast-stats');

Given('the client is authenticated', async function () {
  await this.driver.login('user', 'password');
});

Given('there are no ryvrs configured', async function () {
  // nothing to do as this is the default
});

When('a request is made to the server\'s base URL', async function () {
  this.root = await this.driver.getRoot();
});

const RELS_RYVRS_COLLECTION = 'https://mountain-pass.github.io/ryvr/rels/ryvrs-collection';

When('the ryvrs list is retrieved', async function () {
  this.root = await this.driver.getRoot();
  this.ryvrs = await this.root.getRyvrs();
});

When('the ryvrs list is retrieved directly', async function () {
  this.ryvrs = await this.driver.getRyvrsDirectly();
});

When('the {string} ryvr is retrieved', async function (title) {
  try {
    this.root = await this.driver.getRoot();
    this.ryvrs = await this.root.getRyvrs();
    const ryvr = await this.ryvrs.getRyvr(this.normTitle(title));
    // before we start doing anything with the ryvr, let's make sure it's finished slurping
    // up the data for the current and next page
    console.log('Waiting for ryvr to charge');
    await ryvr.initPromise;
    console.log('charged');
    this.currentRyvr = ryvr;
  } catch (err) {
    this.currentError = err;
  }
});

When('the {string} ryvr is retrieved directly', async function (title) {
  try {
    const ryvr = await this.driver.getRyvrDirectly(this.normTitle(title));
    console.log('Waiting for ryvr to charge');
    await ryvr.initPromise;
    console.log('charged');
    this.currentRyvr = ryvr;
  } catch (err) {
    this.currentError = err;
  }
});


When('a request is made for the API Docs', async function () {
  this.apiDocs = await this.driver.getRelated(this.root, 'describedby');
});

When('the {string} rvyr is deleted', async function (title) {
  const normalizedTitle = this.normTitle(title);
  delete this.ryvrApp.getRyvrs().deleteRyvr(normalizedTitle);
});

When('{int}th record of the {string} ryvr is retrieved', async function (index, title) {
  try {
    this.root = await this.driver.getRoot();
    this.ryvrs = await this.root.getRyvrs();
    this.currentRyvr = await this.ryvrs.getRyvr(this.normTitle(title));
    const iterator = await this.currentRyvr.seek(index);
    console.log('iterator', iterator);
    throw new PendingError();
  } catch (err) {
    if (err instanceof PendingError) {
      throw err;
    }
    this.currentError = err;
  }
});

When('{int}th page of the {string} ryvr is retrieved', async function (page, title) {
  try {
    this.currentRyvr = await this.driver.getRyvrDirectly(this.normTitle(title), page);
  } catch (err) {
    this.currentError = err;
  }
});

const hrTimeToMs = hrTime => hrTime[0] * 1000 + hrTime[1] / 1000000;

When('all the events are retrieved', { timeout: 300000 }, async function () {
  if (this.currentError) { console.log(this.currentError); }
  expect(this.currentError).to.be.undefined;
  expect(this.currentRyvr).to.not.be.undefined;


  this.timeSamples = [];
  this.durations = [];
  const iterator = this.currentRyvr.seek(0);
  this.recordsLoaded = 0;
  let duration = 0;
  this.dataTransferred = 0;
  let before = process.hrtime();
  let { done } = await iterator.next();
  let after = process.hrtime();
  duration = hrTimeToMs(after) - hrTimeToMs(before);
  this.durations.push(duration);
  this.recordsLoaded += 1;

  while (!done) {
    before = process.hrtime();
    const next = await iterator.next();
    after = process.hrtime();
    // eslint-disable-next-line prefer-destructuring
    done = next.done;
    if (!done) {
      duration = hrTimeToMs(after) - hrTimeToMs(before);
      if (duration > 100) {
        console.log('long loadtime', duration, this.recordsLoaded);
      }
      // expect(duration).to.be.lessThan(500);
      this.durations.push(duration);
      this.recordsLoaded += 1;
    }
    if (this.recordsLoaded > 1000100) {
      throw new Error('Whoa! Too many reords');
    }
    if (this.recordsLoaded % this.exposedPageSize === this.exposedPageSize - 1) {
      // careful not to remove this next line, otherwise it will be optimised away
      // and we will not yeild to give the ryvr a change to load.
      process.stdout.write('.');
      // need to yeild here to simulate the route returning
      await new Promise((resolve) => {
        setTimeout(() => resolve(), 10);
      });
    }
  }


  await this.currentRyvr.dataTransferredPromise;
  this.dataTransferred = this.currentRyvr.dataTransferred;

  this.durationStats = new Stats().push(this.durations);
  this.totalDuration = (this.durations.reduce((a, b) => a + b, 0)) * 0.001;
  expect(this.recordsLoaded).to.equal(this.expectedRecordCount);
});

Then(
  'the root entity will have an application name of {string}',
  async function (title) {
    expect(this.root.title).to.equal(title);
  },
);

Then('the root entity will contain a link to the api-docs', async function () {
  expect(this.driver.hasLink(this.root, 'describedby')).to.be.true;
});


Then('the root entity will contain a link to the ryvrs', async function () {
  expect(this.driver.hasLink(this.root, RELS_RYVRS_COLLECTION)).to.be.true;
});

Then('the ryvrs list will be empty', async function () {
  expect(this.ryvrs).to.be.an('object');
  expect(this.ryvrs.keys).to.be.empty;
});

Then('the count of ryvrs will be {int}', async function (length) {
  expect(this.ryvrs.length).to.equal(length);
});

Then('the ryvrs list will contain the following entries', async function (dataTable) {
  // make the name unique for this scenario, to prevent conflicts with other tests
  const names = dataTable.raw()[0].map(title => this.normTitle(title));
  expect(this.ryvrs.keys).to.have.members(names);
});


Then('the ryvr will not be found', async function () {
  expect(this.currentError).to.not.be.undefined;
  expect(this.currentRyvr).to.be.undefined;
  expect(this.driver.getErrorMsg(this.currentError)).to.equal('Not Found');
});

Then('the record will not be found', async function () {
  expect(this.currentRecord).to.be.undefined;
  expect(this.currentError).to.not.be.undefined;
  expect(this.driver.getErrorMsg(this.currentError)).to.equal('Not Found');
});

Then('the page will not be found', async function () {
  expect(this.currentError).to.not.be.undefined;
  expect(this.driver.getErrorMsg(this.currentError)).to.equal('Not Found');
});


Then('it will contain exactly', async function (dataTable) {
  const expectedRows = dataTable.hashes().map((row) => {
    const rval = Object.assign({}, row);
    if (rval.id !== undefined) {
      rval.id = parseInt(rval.id, 10);
    }
    if (rval.amount !== undefined) {
      rval.amount = parseFloat(rval.amount);
    }
    return rval;
  });
  if (this.currentError) {
    console.log(this.currentError);
    throw this.currentError;
  }
  expect(this.currentError).to.be.undefined;
  expect(this.currentRyvr).to.not.be.undefined;
  const rows = [];
  for await (const value of this.currentRyvr) {
    rows.push(value);
  }
  expect(rows.length).to.equal(expectedRows.length);
  expect(rows).to.deep.equal(expectedRows);
});

Then('it will have the following structure', async function (dataTable) {
  const headings = dataTable.raw()[0];
  console.log('CURRENT ERR', this.currentError);
  expect(this.currentError).to.be.undefined;
  expect(this.currentRyvr).to.not.be.undefined;
  console.log('GETTING fields');
  const fields = await this.currentRyvr.getFields();
  console.log('received fields', fields);
  expect(fields).to.deep.equal(headings);
});


Then('it will have {int} events', async function (noEvents) {
  expect(this.currentError).to.be.undefined;
  expect(this.currentRyvr).to.not.be.undefined;
  console.log('Getting events');
  const rows = [];
  for await (const value of this.currentRyvr) {
    console.log('got row', value);
    rows.push(value);
    if (rows.length > 100) {
      console.log('too many');
      process.exit(1);
    }
  }
  expect(rows.length).to.equal(noEvents);
});


Then('the median record should load within {float}µs', async function (maxµs) {
  console.log('medianPageLoadTime', this.durationStats.median());
  expect(this.durationStats.median() * 1000).to.be.lessThan(maxµs);
});

Then('{int}% of the records should each load within {float}µs', async function (percentile, maxµs) {
  console.log(`${percentile} pageLoadTime`, this.durationStats.percentile(percentile));
  expect(this.durationStats.percentile(percentile) * 1000).to.be.lessThan(maxµs);
});

Then('{int}% of the records should each load within {int}ms', async function (percentile, maxMs) {
  console.log(`${percentile} pageLoadTime`, this.durationStats.percentile(percentile));
  expect(this.durationStats.percentile(percentile)).to.be.lessThan(maxMs);
});


Then('the event retrieval throughput should be at least {float}MB per s', async function (minMBperS) {
  console.log('total duration (s)', this.totalDuration);
  const megaBytes = (this.dataTransferred / 1024 / 1024);
  console.log('total size (MB)', megaBytes);
  const actualMBperS = megaBytes / this.totalDuration;
  console.log('MB/s', actualMBperS);
  expect(actualMBperS).to.be.greaterThan(minMBperS);
});

Then('the event retrieval rate should be at least {int}TPS', async function (minTPS) {
  console.log('records', this.recordsLoaded);
  const actualTPS = this.recordsLoaded / this.totalDuration;
  console.log('TPS', actualTPS);
  expect(actualTPS).to.be.greaterThan(minTPS);
});
