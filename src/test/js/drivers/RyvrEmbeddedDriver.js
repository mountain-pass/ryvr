

import { RyvrDriver } from './RyvrDriver';

const RELS_RYVRS_COLLECTION = 'https://mountain-pass.github.io/ryvr/rels/ryvrs-collection';


class RyvrEmbeddedDriver extends RyvrDriver {
  constructor(ryvrApp) {
    super();
    this.ryvrApp = ryvrApp;
  }


  async getRoot() {
    return this.ryvrApp;
  }

  async login(username, password) {
    this.username = username;
    this.password = password;
  }


  async getRyvrsDirectly() {
    // in the rest client, this would be done by going straight to the url, rather than
    // following liks.
    return this.ryvrApp.getRyvrs();
  }

  async getRyvrDirectly(title, page = 1) {
    if (page < 1) {
      throw new Error('Not Found');
    }
    const ryvrs = await this.ryvrApp.getRyvrs();
    return ryvrs.getRyvr(title);
  }

  hasLink(obj, rel) {
    switch (obj.constructor.name) {
      case 'RyvrApp':
        switch (rel) {
          case 'describedby':
            return obj.getApiDocs !== undefined;
          case RELS_RYVRS_COLLECTION:
            return obj.getRyvrs !== undefined;

          default:
            return false;
        }

      default:
        return false;
    }
  }

  getErrorMsg(err) {
    return err.message;
  }
}

module.exports = RyvrEmbeddedDriver;
