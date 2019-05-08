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

  fastify.get('/ryvrs/:title', async (request, reply) => {
    console.log('PAGE', request.query.page);

    if (request.query.page < 1) {
      reply.code(404).send({ message: 'Not Found' });
      return;
    }

    try {
      const links = new LinkHeader();
      links.set({ rel: 'self', uri: `/ryvrs/${request.params.title}` });

      const ryvr = ryvrApp.getRyvrs().getRyvr(request.params.title);

      const body = [];
      // eslint-disable-next-line no-restricted-syntax
      for await (const row of ryvr) {
        body.push(row);
      }

      reply
        .code(200)
        .header('link', links.toString())
        .send(body);
    } catch (err) {
      if (err.message === 'Not Found') {
        reply.code(404).send({ message: 'Not Found' });
      } else {
        reply
          .code(500)
          .send({ message: 'Infernal Server Error' });
      }
    }
  });
}

module.exports = routes;
