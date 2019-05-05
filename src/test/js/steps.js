import { Given, Then, When } from 'cucumber';

Given('the client is authenticated', async function () {
  await this.client.login('user', 'password');
});

Given('there are no ryvrs configured', async function () {
  // nothing to do as this is the default
});

When('a request is made to the server\'s base URL', async function () {
  this.root = await this.client.getRoot();
});

When('the ryvrs list is retrieved', async function () {
  this.ryvrs = await this.client.getRyvrs();
});

When('the ryvrs list is retrieved directly', async function () {
  this.ryvrs = await this.client.getRyvrsDirectly();
});

When('the {string} ryvr is retrieved', async function (ryvr) {
  this.currentRyvr = await this.client.getRyvr(ryvr);
});

When('a request is made for the API Docs', async function () {
  this.apiDocs = await this.root.getApiDocs();
});


Then(
  'the root entity will have an application name of {string}',
  async function (title) {
    expect(this.root.title).to.equal(title);
  },
);

Then('the root entity will contain a link to the api-docs', async function () {
  expect(this.root).to.have.a.property('getApiDocs');
  expect(this.root.getApiDocs).to.be.a('function');
});


Then('the root entity will contain a link to the ryvrs', async function () {
  expect(this.root).to.have.a.property('getRyvrs');
  expect(this.root.getRyvrs).to.be.a('function');
});

Then('the ryvrs list will be empty', async function () {
  expect(this.ryvrs).to.be.an('object');
  expect(Object.keys(this.ryvrs)).to.be.empty;
});

Then('the count of ryvrs will be {int}', async function (length) {
  expect(Object.keys(this.ryvrs).length).to.equal(length);
});

Then('the ryvrs list will contain the following entries', async function (dataTable) {
  // make the name unique for this scenario, to prevent conflicts with other tests
  const names = dataTable.raw()[0].map(name => `${name}-${this.scenarioId}`);
  expect(this.ryvrs).to.have.keys(names);
});


Then('the ryvr will not be found', async function () {
  expect(this.currentRyvr).to.be.undefined;
});
