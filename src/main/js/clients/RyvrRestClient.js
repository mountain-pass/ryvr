import got from 'got';
import LinkHeader from 'http-link-header';

const map = new Map();


class RyvrRestClient {
  constructor(url) {
    this.url = url;
    this.defaultGotOptions = {
      baseUrl: this.url,
      cache: map,
    };
    this.got = got.extend(this.defaultGotOptions);
  }


  async getRoot() {
    return this.got('/');
  }

  async login(username, password) {
    this.username = username;
    this.password = password;
    this.got = got.extend(Object.assign({}, this.defaultGotOptions, {
      auth: `${username}:${password}`,
    }));
    const response = await this.getRoot();
    return response;
  }

  getLink(response, rel, title = undefined) {
    if (response.headers.link === undefined) {
      throw new Error('Not Found');
    }
    const links = LinkHeader.parse(response.headers.link).get('rel', rel);
    return title !== undefined ? links.find(l => l.title === title) : links[0];
  }


  hasLink(response, rel, title = undefined) {
    if (response.headers.link === undefined) {
      return false;
    }
    if (title === undefined) {
      return LinkHeader.parse(response.headers.link).has('rel', rel);
    }

    const links = LinkHeader.parse(response.headers.link).get('rel', rel);
    return links.find(l => l.title === title) !== undefined;
  }

  async getRelated(response, rel, title = undefined) {
    const link = this.getLink(response, rel, title);
    if (link) {
      return this.got(link.uri);
    }

    throw new Error('Not Found');
  }


  async getRelatedStream(response, rel, title = undefined) {
    const link = this.getLink(response, rel, title);
    if (link) {
      return this.got.stream(link.uri);
    }

    throw new Error('Not Found');
  }
}

module.exports = RyvrRestClient;
