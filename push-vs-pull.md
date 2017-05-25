---
title: 'Push vs Pull'
---

| Feature    | ryvr | Push |
| ---------- | ---- | ---- |
| Throttling | Clients only receive new events when they request it. When there is a spike in the number of new events, ryvr clients will gracefully fall behind and catch up when the spike subsides. | Clients are expected to consume the events are they become available. When there is a spike in the number of new events, push based clients can drop messages or run-out of resources such as memory or threads |


