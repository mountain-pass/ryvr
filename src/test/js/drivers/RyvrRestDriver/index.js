import LinkHeader from 'http-link-header';
import RyvrRestClient from '../../../../main/js/clients/RyvrRestClient';

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

class RemoteRvyr extends RemoteResource {
  seek(i) {
    const ryvr = this;
    if (ryvr.results === undefined) {
      ryvr.results = JSON.parse(this.response.body);
    }
    let index = i;
    if (index < 0) {
      throw new Error('Not Found');
    }
    const iterator = {
      next: async () => {
        if (index >= ryvr.results.length) {
          return { done: true };
        }
        const value = ryvr.results[index];
        index += 1;
        return { value, done: false };
      },
    };

    return iterator;
  }

  [Symbol.asyncIterator]() {
    return this.seek(0);
  }
}

class RemoteRyvrsCollection extends RemoteResource {
  async getRyvr(title) {
    return new RemoteRvyr(this.client, await this.client.getRelated(this.response, 'item', title));
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
    const response = await this.client.got(`/ryvrs/${title}?page=${page}`);
    console.log('RESPONSE', response);
    if (response.statusCode === 404) {
      throw new Error('Not Found');
    }
    return new RemoteRvyr(this.client, response);
  }

  getErrorMsg(err) {
    return err.body ? JSON.parse(err.body).message : err.message;
  }

  hasLink(remoteResource, rel) {
    return remoteResource.links.has('rel', rel);
  }
}

module.exports = RyvrRestDriver;
