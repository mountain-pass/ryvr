
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
}

exports.RyvrClient = RyvrClient;
