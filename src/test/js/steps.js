/* eslint-disable no-restricted-syntax */
import { PendingError } from '@windyroad/cucumber-js-throwables/lib/pending-error';
import { Given, Then, When } from 'cucumber';


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
    this.currentRyvr = await this.ryvrs.getRyvr(this.normTitle(title));
  } catch (err) {
    this.currentError = err;
  }
});

When('the {string} ryvr is retrieved directly', async function (title) {
  try {
    this.currentRyvr = await this.driver.getRyvrDirectly(this.normTitle(title));
    await new Promise((resolve, reject) => {
      this.currentRyvr.getStream().on('error', (err) => {
        reject(err);
      }).on('end', () => { resolve(); });
    });
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
  expect(this.currentRyvr).to.be.undefined;
  expect(this.currentError).to.not.be.undefined;
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
  expect(this.currentError).to.be.undefined;
  expect(this.currentRyvr).to.not.be.undefined;
  const rows = [];
  for await (const value of this.currentRyvr) {
    rows.push(value);
  }
  expect(rows).to.deep.equal(expectedRows);
});
