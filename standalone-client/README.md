# Student Housing — Client (Week 10)

A standalone front-end (no framework) that consumes the REST API of the Student Housing
Spring Boot backend. Built with **npm + webpack**, styled with **Sass + customized Bootstrap**,
linted with **ESLint**, formatted with **dprint**.

## Features

Single HTML page with a navbar (**Search** / **Add**). Navigation is a hand-written SPA: clicking a
menu item shows one `<section>` and hides the other via `element.style.display` (no Bootstrap JS).

- **Search** — `GET /api/students?name=<term>`. Renders results in a table, or "No records were
  found." when empty. Uses `fetch` with `async/await`.
- **Add** — `POST /api/units`. On success the form is cleared and a message is shown; on failure the
  input is kept for resubmission. Uses `fetch` with `async/await`.

## Prerequisites

- Node.js + npm
- The Spring Boot backend running on `http://localhost:8080` (its `/api/units` POST is `permitAll`
  with CSRF disabled, and CORS allows this client's origin `http://localhost:9000`).

## Run

```bash
npm install
npm start        # webpack dev server on http://localhost:9000 (opens the browser)
```

Other scripts:

```bash
npm run build    # production build into dist/
npm run lint     # ESLint
npm run format   # dprint fmt
```

## Project structure

```
src/
  index.html        # single page: navbar + Search/Add sections
  index.js          # entry: loads styles, initializes features
  scss/main.scss    # customized Bootstrap + custom SCSS (variables, mixin, nesting)
  js/
    navigation.js   # SPA show/hide logic (own code)
    api.js          # backend base URL
    search.js       # search fetch (async/await)
    add.js          # add fetch (async/await)
```
