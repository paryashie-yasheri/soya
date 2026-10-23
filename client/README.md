# SOA web client

Static HTML/CSS/JavaScript interface with a small PHP same-origin proxy. The browser only calls `api.php`; PHP forwards allowlisted requests to the HTTPS Bands and Grammy APIs on Helios. The proxy verifies TLS using the public self-signed service certificates stored beside it, so the page needs neither browser CORS exceptions nor accepting the services' certificates in the user's browser.

The interface covers every operation in `../openapi.yaml`: Bands CRUD, collection paging/filtering/sorting, search, average/count operations, and both Grammy POST operations. Grammy does not expose read/list operations for its saved records, so the UI shows returned operation results in browser-local history instead of inventing an endpoint. A read-only check found the deployed Bands service currently ignores collection filter parameters; the client still sends the documented query and applies exact filters locally over paged collection results so the visible filter controls work without changing Bands.

Deploy the contents of this directory to a directory under `~/public_html/` on Helios. Apache must execute PHP 8 with the OpenSSL extension and `allow_url_fopen=On` (the current Helios vhost advertises PHP 8.2).
