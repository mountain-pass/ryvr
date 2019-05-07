import LinkHeader from 'http-link-header';

const RELS_RYVRS_COLLECTION = 'https://mountain-pass.github.io/ryvr/rels/ryvrs-collection';

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
        pageSize: ryvr.pageSize,
      };
      links.set({ rel: 'item', uri: `/ryvrs/${key}?page=1`, title: key });
    });
    reply
      .code(200)
      .header('link', links.toString())
      .send(body);
  });
}

module.exports = routes;
