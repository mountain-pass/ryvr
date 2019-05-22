import LinkHeader from 'http-link-header';
import RyvrRestClient from '../../../../main/js/clients/RyvrRestClient';
import { Ryvr, RyvrPage } from '../../../../main/js/model/Ryvr';

const RELS_RYVRS_COLLECTION = 'https://mountain-pass.github.io/ryvr/rels/ryvrs-collection';

class RemoteResource {
  constructor(client, response) {
    this.client = client;
    this.response = response;
    this.theBody = undefined;
    this.theLinks = undefined;
  }

  get body() {
    if (this.theBody === undefined) {
      this.theBody = JSON.parse(this.response.body);
    }
    return this.theBody;
  }

  get links() {
    if (this.theLinks === undefined) {
      this.theLinks = LinkHeader.parse(this.response.headers.link);
    }
    return this.theLinks;
  }
}

class RestRyvrPage extends RyvrPage {
  constructor(client, r) {
    super();
    this.client = client;
    this.response = r;
    this.headers = r.headers;
    this.underlyingPageSize = r.headers['page-size'];
    this.pageNo = r.headers.page;
    this.cachedRows = JSON.parse(r.body);
    this.itemsPromise = Promise.resolve(this.cachedRows);
    /* finishing init */


    this.queryPromise = Promise.resolve(this.cachedRows);

    this.fields = JSON.parse(r.headers.fields);
    this.fieldsPromise = Promise.resolve(this.fields);
    this.ended = true;

    this.endPromise = Promise.resolve(this);
  }

  async loadNextPage() {
    const next = this.client.getLink(this.response, 'next').uri;
    const nextPage = await RestRyvrPage.loadPage(this.client, next);
    return nextPage;
  }

  async hasNextPage() {
    const next = this.client.hasLink(this.response, 'next');
    return next;
  }


  nextRowLoaded() {
    const rowLoaded = new Promise((resolve) => {
      this.stream.once('result', () => {
        resolve();
      });
    });
    return Promise.race([rowLoaded, this.endPromise, this.errorPromise]);
  }

  async getUnderlyingPageSize() {
    return this.underlyingPageSize;
  }

  async getSizeInBytes() {
    return parseInt(this.headers['content-length'], 10);
  }
}

RestRyvrPage.loadPage = async function (client, link) {
  try {
    const pageRespone = await client.got(link);
    return new RestRyvrPage(client, pageRespone);
  } catch (err) {
    console.error('ERROR', err.message);
    throw err;
  }
};

class RemoteRyvrsCollection extends RemoteResource {
  async getRyvr(title) {
    const page = await RestRyvrPage.loadPage(this.client, this.client.getLink(this.response, 'item', title).uri);
    return new Ryvr(this.client, 8192, page);
  }

  get keys() {
    return Object.keys(this.body);
  }

  get length() {
    return this.keys.length;
  }
}

class RemoteRoot extends RemoteResource {
  async getRyvrs() {
    return new RemoteRyvrsCollection(this.client,
      await this.client.getRelated(this.response, RELS_RYVRS_COLLECTION));
  }

  get title() {
    return this.body.title;
  }
}

class RyvrRestDriver {
  constructor(url) {
    this.client = new RyvrRestClient(url);
  }


  async getRoot() {
    return new RemoteRoot(this.client, await this.client.getRoot());
  }

  async login(username, password) {
    return new RemoteRoot(this.client.login(username, password));
  }


  // async getRyvrs() {
  //   const root = await this.getRoot();
  //   if (root.headers.link === undefined) {
  //     throw new Error('NotFound');
  //   }
  //   const links = LinkHeader.parse(root.headers.link);
  //   if (!links.has('rel', RELS_RYVRS_COLLECTION)) {
  //     throw new Error('NotFound');
  //   }
  //   const link = links.get('rel', RELS_RYVRS_COLLECTION);
  //   const response = await this.got(link[0].uri);
  //   return response;
  // }

  // async getRyvr(title) {
  //   const ryvrs = await this.getRyvrs();
  //   if (ryvrs.headers.link === undefined) {
  //     throw new Error('NotFound');
  //   }
  //   const links = LinkHeader.parse(ryvrs.headers.link);
  //   if (!links.has('rel', RELS_RYVRS_COLLECTION)) {
  //     throw new Error('NotFound');
  //   }
  //   const link = links.get('rel', RELS_RYVRS_COLLECTION).find(l => l.title === title);
  //   process.exit(1);
  // }


  async getRyvrsDirectly() {
    const response = await this.client.got('/ryvrs');
    return new RemoteRyvrsCollection(this.client, response);
  }

  async getRyvrDirectly(title, page = 1) {
    const ryvrpage = await RestRyvrPage.loadPage(this.client, `/ryvrs/${title}?page=${page}`);
    return new Ryvr(this.client, 8192, ryvrpage);
  }

  getErrorMsg(err) {
    return err.body ? JSON.parse(err.body).message : err.message;
  }

  hasLink(remoteResource, rel) {
    return remoteResource.links.has('rel', rel);
  }
}

module.exports = RyvrRestDriver;
