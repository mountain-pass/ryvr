import assert from 'assert';
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
  constructor(client, response) {
    super(client, response);
    console.log(response.headers);
    this.fields = JSON.parse(response.headers.fields);
  }

  seek(i) {
    const ryvr = this;
    if (ryvr.results === undefined) {
      // should we do this parsing here, or later, when we actually use the value?
      ryvr.results = JSON.parse(this.response.body);
      ryvr.upstreamPageSize = this.response.headers['page-size'];
      ryvr.upstreamCurrentPageSize = this.response.headers['current-page-size'];
      ryvr.upstreamPage = this.response.headers.page;
    }
    const index = i;
    if (index < 0) {
      throw new Error('Not Found');
    }
    let adjustedIndex = index - (ryvr.upstreamPageSize * (ryvr.upstreamPage - 1));
    const iterator = {
      next: async () => {
        // if page is >= 10, then the loop is borken (for now)
        while (adjustedIndex >= ryvr.upstreamPageSize && ryvr.upstreamPage < 10) {
          // can't assume we are done
          // need to fetch next page.
          if (this.client.hasLink(ryvr.response, 'next')) {
            // eslint-disable-next-line no-await-in-loop
            const nextPage = await this.client.getRelated(ryvr.response, 'next');
            ryvr.response = nextPage;
            // definiatly shouldn't parse the results here
            ryvr.results = JSON.parse(this.response.body);
            ryvr.upstreamPageSize = this.response.headers['page-size'];
            ryvr.upstreamCurrentPageSize = this.response.headers['current-page-size'];
            ryvr.upstreamPage = this.response.headers.page;
            console.log(`GOT PAGE ${ryvr.upstreamPage} (size ${ryvr.upstreamCurrentPageSize})`);
            console.log(`old adjustedIndex = ${adjustedIndex}`);
            adjustedIndex -= ryvr.upstreamPageSize;
            console.log(`new adjustedIndex = ${adjustedIndex}`);
            assert(adjustedIndex === 0, `adjustedIndex should be 0, but it's ${adjustedIndex}. rl = ${ryvr.results.length}`);
          } else {
            console.log('NO NEXT PAGE');
            return { done: true };
          }
        }
        if (adjustedIndex >= ryvr.upstreamCurrentPageSize) {
          return { done: true };
        }
        const value = ryvr.results[adjustedIndex];
        adjustedIndex += 1;
        return { value, done: false };
      },
    };

    return iterator;
  }

  [Symbol.asyncIterator]() {
    return this.seek(0);
  }

  async getFields() {
    return this.fields;
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
