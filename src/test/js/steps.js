import { Given, Then, When } from 'cucumber';

Given('the client is authenticated', async function () {
  console.log('world', this);
  this.root = await this.client.getRoot();
  this.root.login('user', 'password');
});

Given('there are no ryvrs configured', async function () {
  // nothing to do as this is the default
});

When('a request is made to the server\'s base URL', async function () {
  this.root = await this.client.getRoot();
});

When('the ryvrs list is retrieved', async function () {
  this.ryvrs = await this.root.getRyvrs();
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
  expect(this.ryvrs).to.be.an('array');
  expect(this.ryvrs).to.be.empty;
});

Then('the count of ryvrs will be {int}', async function (length) {
  expect(this.ryvrs.length).to.equal(length);
});