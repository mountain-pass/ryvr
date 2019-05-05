
class RyvrClient {
  constructor(ryvrApp) {
    this.ryvrApp = ryvrApp;
  }


  async getRoot() {
    return this.ryvrApp;
  }

  async login(username, password) {
    this.ryvrApp.login(username, password);
  }

  async getRyvrs() {
    return this.ryvrApp.getRyvrs();
  }

  async getRyvr(title) {
    return this.ryvrApp.getRyvr(title);
  }


  async getRyvrsDirectly() {
    // in the rest client, this would be done by going straight to the url, rather than
    // following liks.
    return this.ryvrApp.getRyvrs();
  }
}

exports.RyvrClient = RyvrClient;
