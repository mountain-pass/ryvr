import LinkHeader from 'http-link-header';


const RELS_RYVRS_COLLECTION = 'https://mountain-pass.github.io/ryvr/rels/ryvrs-collection';
const RELS_PAGE = 'https://mountain-pass.github.io/ryvr/rels/page';


const currentPageMaxAge = 1;
const archivePageMaxAge = 31536000;

async function routes(fastify, options) {
  const { ryvrApp } = options;

  fastify.get('/', async (request, reply) => {
    const links = new LinkHeader();
    links.set({ rel: 'self', uri: '/' });
    links.set({ rel: RELS_RYVRS_COLLECTION, uri: '/ryvrs' });
    links.set({ rel: 'describedby', uri: '/api-docs' });
    reply
      .code(200)
      .header('link', links.toString())
      .send({ title: 'ryvr' });
  });

  fastify.get('/ryvrs', async (request, reply) => {
    const links = new LinkHeader();
    links.set({ rel: 'self', uri: '/ryvrs' });

    const ryvrs = await ryvrApp.getRyvrs();

    const body = {};
    ryvrs.keys.forEach((key) => {
      const ryvr = ryvrs.getRyvr(key);
      body[key] = {
        title: ryvr.title,
        exposedPageSize: ryvr.exposedPageSize,
      };
      links.set({ rel: 'item', uri: `/ryvrs/${key}?page=1`, title: key });
    });
    reply
      .code(200)
      .header('link', links.toString())
      .send(body);
  });

  fastify.get('/ryvrs/:title', async (request, reply) => {
    // eslint-disable-next-line radix
    const page = parseInt(request.query.page);
    if (page < 1) {
      reply.code(404).send({ message: 'Not Found' });
      return;
    }

    try {
      const links = new LinkHeader();

      const ryvr = ryvrApp.getRyvrs().getRyvr(request.params.title);

      const body = [];
      const { exposedPageSize } = ryvr;
      const iterator = ryvr.seek((request.query.page - 1) * exposedPageSize);
      let { value, done } = await iterator.next();
      for (let i = 0;
        i < exposedPageSize && !done;
        // eslint-disable-next-line no-await-in-loop
        i += 1, { value, done } = await iterator.next()) {
        body.push(value);
      }

      const base = `/ryvrs/${request.params.title}`;
      links.set({ rel: 'self', uri: `${base}?page=${page}` });
      links.set({ rel: 'first', uri: `${base}?page=1`, title: 'First' });
      if (page > 1) {
        links.set({ rel: 'prev', uri: `${base}?page=${page - 1}`, title: 'Prev' });
      }

      const linkTemplate = `<${base}?page={page}>; rel="${RELS_PAGE}"; var-base="https://mountain-pass.github.io/ryvr/vars/"`;
      reply.header('Link-Template', linkTemplate);


      const isLastPage = done;


      if (isLastPage) {
        reply.header('cache-control', `max-age=${currentPageMaxAge}`);
        const pageRecordCount = body.length;
        reply.header('etag', `"${page}.${pageRecordCount}"`);
        reply.header('current-page-size', pageRecordCount);
        reply.header('archive-page', 'false');
      } else {
        reply.header('cache-control', `max-age=${archivePageMaxAge}`);
        reply.header('etag', `"${page}"`);
        reply.header('current-page-size', exposedPageSize);
        reply.header('archive-page', 'true');
        links.set({ rel: 'next', uri: `${base}?page=${page + 1}`, title: 'Next' });
      }
      reply.header('Page', page);
      reply.header('Page-Size', exposedPageSize);
      const fields = await ryvr.getFields();
      reply.header('fields', JSON.stringify(fields));

      reply
        .code(200)
        .header('link', links.toString())
        .send(body);
    } catch (err) {
      console.error(err);
      if (err.message === 'Not Found') {
        reply.code(404).send({ message: 'Not Found' });
      } else if (err instanceof PendingError) {
        reply.code(501).send({ message: err.message });
      } else {
        reply
          .code(500)
          .send({ message: 'Infernal Server Error' });
      }
    }
  });
}

module.exports = routes;
