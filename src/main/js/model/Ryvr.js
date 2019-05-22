/* eslint-disable no-await-in-loop */

class RyvrPage {
  constructor() {
    this.cachedRows = [];
    this.ended = false;
  }

  async loadNextPage() {
    throw Error('Not Implemented');
  }

  nextRowLoaded() {
    const rowLoaded = new Promise((resolve) => {
      this.stream.once('result', () => {
        resolve();
      });
    });
    return Promise.race([rowLoaded, this.endPromise, this.errorPromise]);
  }
}

// const hrTimeToMs = hrTime => hrTime[0] * 1000 + hrTime[1] / 1000000;

class Ryvr {
  constructor(title, exposedPageSize, firstpage) {
    this.theTitle = title;
    this.theExposedPageSize = exposedPageSize;
    this.underlyingPage = 1;
    this.dataTransferred = 0;
    this.fields = [];


    /*

    So I've just learnt that the JDBC client allows seeking by loading all the rows into memory :(
    We'll need to be clever with LIMIT and OFFSET and have a set of queries we can execute
    which load the data into a reusable buffer
    When we seek past the end of the buffer, we get the next page

    At least I now understand why it was so fast gettting the next results from the resultSet
    It was just getting them from memory.

    Open question: if we stick to the C API and use mysql_stmt_fetch() or mysql_fetch_row()
    to grab the data in sequence, without having to read pages. We would only need to
    re-execute the query when moving backwards.

    BUT this isn't going to help us in the long run,
    because when we are rendering the current page, we need to requery and then seek to the
    current page, which requires iterating over each record. Much faster (I think) to use LIMIT
    and offset in the query.
  */

    // TODO: support a configureable number of look ahead pages
    this.pages = [];

    this.pages[0] = firstpage;
    this.initPromise = this.pages[0].endPromise
      .then(page => page.loadNextPage()).then((nextPage) => {
        if (nextPage) {
          this.pages[1] = nextPage;
          return this.pages[1].endPromise;
        }
        return this.pages[0].endPromise;
      }).then((rpage) => {
        console.log('ryrv is charged', this.pages[0].constructor.name);
        return rpage;
      }).catch(() => {
        console.log('no next page');
      });
    this.nextPromise = this.initPromise;
    this.dataTransferred = 0;
    this.dataTransferredPromise = this.pages[0].endPromise
      .then(rpage => rpage.getSizeInBytes())
      .then((size) => {
        this.dataTransferred += size;
      });
  }


  seek(i) {
    const ryvr = this;
    let index = i;
    if (index < 0) {
      throw new Error('Not Found');
    }
    const iterator = {
      next: async () => {
        const underlyingPageSize = await ryvr.pages[0].getUnderlyingPageSize();
        let adjustedIndex = index - (underlyingPageSize * (ryvr.underlyingPage - 1));
        while (adjustedIndex >= underlyingPageSize) {
          // const before = process.hrtime();
          // console.log('waiting for hasNextPage...');
          const hasNextPage = await ryvr.pages[0].hasNextPage();
          // const after = process.hrtime();
          // const duration = hrTimeToMs(after) - hrTimeToMs(before);
          // console.log(`done waiting for hasNextPage (${duration}ms)`);
          if (hasNextPage) {
            await ryvr.nextPromise;
            // eslint-disable-next-line prefer-destructuring
            ryvr.pages[0] = ryvr.pages[1];
            ryvr.underlyingPage += 1;
            // console.log('PAGE', ryvr.underlyingPage);
            adjustedIndex = index - (underlyingPageSize * (ryvr.underlyingPage - 1));

            ryvr.nextPromise = ryvr.pages[0].loadNextPage().then((nextPage) => {
              ryvr.pages[1] = nextPage;
              ryvr.pages[1].endPromise
                .then(page => page.getSizeInBytes())
                .then((size) => {
                  this.dataTransferred += size;
                });
            }).catch(() => {
              console.log('no next page', ryvr.underlyingPage, ryvr.pages[0].pageNo);
            });
            // try {
            //   const nextPage = await ryvr.pages[0].loadNextPage();
            //   ryvr.pages[1] = nextPage;
            //   ryvr.pages[1].endPromise
            //     .then(page => page.getSizeInBytes())
            //     .then((size) => {
            //       this.dataTransferred += size;
            //     });
            // } catch (err) {
            //   console.log('no next page', ryvr.underlyingPage, ryvr.pages[0].pageNo);
            // }
          } else {
            console.log('reached end of pages');
            return { done: true };
          }
        }
        if (adjustedIndex >= ryvr.pages[0].cachedRows.length) {
          if (ryvr.pages[0].ended) {
            return { done: true };
          }

          // console.log('waiting for next row', index,
          // ryvr.pages[0].cachedRows.length, ryvr.pages[0].constructor.name);
          // const s = process.hrtime();
          await ryvr.pages[0].nextRowLoaded();
          // const e = process.hrtime();
          // console.log(`done waiting (${hrTimeToMs(e) - hrTimeToMs(s)}ms)`);
          if (adjustedIndex >= ryvr.pages[0].cachedRows.length && ryvr.pages[0].ended) {
            console.log('reached end of pages');
            return { done: true };
          }
        }
        const value = ryvr.pages[0].cachedRows[adjustedIndex];
        if (index % 10000 === 0) {
          console.log('returning', value);
        }

        index += 1;
        return { value, done: false };
      },

    // hasNext: async () => {
    //   await ryvr.queryPromise;
    //   console.log('INDEX', index);
    //   console.log('ryvr.cachedRows.length', ryvr.cachedRows.length);
    //   return (index - 1) < ryvr.cachedRows.length;
    // },
    };

    return iterator;
  }

  [Symbol.asyncIterator]() {
    return this.seek(0);
  }

  async getFields() {
    return this.pages[0].fieldsPromise;
  }

  get title() {
    return this.theTitle;
  }

  get exposedPageSize() {
    return this.theExposedPageSize;
  }

  get underlyingPageSize() {
    return this.theUnderlyingPageSize;
  }
}


exports.Ryvr = Ryvr;
exports.RyvrPage = RyvrPage;
