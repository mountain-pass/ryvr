* Ryvrs now use [Link headers](https://tools.ietf.org/html/rfc5988#page-6) instead of HAL as this allows navigation without having to parse the body of the response
* Ryvrs no longer use Jackson for serialisation as much faster serialisation can be achieved with a custom serialiser than a generic serialiser
* Ryvrs now provide appropriate caching headers