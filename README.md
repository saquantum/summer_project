# Summer Project Front-end

This is the front-end of the summer project, detail information is in the main branch.

## Development Setup

As this is the front-end of the project, this part will not cover how to start the entire project, click this link if you do want to. This guide will help you set up a complete development environment for this project

### Required software

- **Node.js 20+** - [Download Node.js](https://nodejs.org/)
- **pnpm** - you can install it with `npm install -g pnpm` after Node.js is installed
- **Git** - [Download Git](https://git-scm.com/)

### Source Code Setup

#### Clone Repository

```
# Clone the main repository
git clone git@github.com:saquantum/summer_project.git
cd summer_project/vue
```

If your prefer working without backend code, there is a copy of the front-end source code

```
git clone git@github.com:redial17/summer_project_frontend.git
cd summer_project_front_end
```

#### Install dependencies

```sh
pnpm install
```

#### Compile and Hot-Reload for Development

```sh
pnpm dev
```

#### Compile and Minify for Production

```sh
pnpm build
```

#### Project Structure

- `/api`: Contains abstracted API communication layers for backend integration
- `/assets`: Contains static resources including images, stylesheets, and configuration files
- `/components`: Contains reusable UI components following the single responsibility principle
- `/composables`: Contains reactive composition functions for shared business logic
- `/router`: Contains application routing logic and navigation guards
- `/stores`: Contains centralized state management using Pinia
- `/types`: Contains TypeScript interface definitions and type declarations
- `/views`: Contains page-level components corresponding to application routes
- `/utils`: Contains utility functions and helper methods for cross-cutting concerns
- `/tests`: Contains unit test specifications
- `/cypress`: Contains end-to-end testing suites

### Docker

You can build the front-end alone in the root folder with:

```sh
docker build -t myapp .
```

To run the whole project, you need to start the backend server first. Further information is available in the main branch.

# Testing

You can run all unit tests with:

```shell
pnpm test
```

---

To run the E2E tests, you need to start the web application first. Further information is available in the main branch. Once the server is ready, you can run all E2E tests. The commands are as follows:

Run all E2E tests with:

```shell
pnpm cy:run
```

If you prefer a GUI, add `--headed`:

```shell
pnpm cy:run --headed
```

You can also run tests manually using the Cypress GUI:

```shell
pnpm cy:open
```

### Configuration

#### Image upload

The image upload function is implemented in the utils folder. It uses an environment file for configuration.

Under development environment, the following configuration is in `.env.development` file. It use a free image server, so the server only takes 10 images a day.

```
# Upload Configuration
VITE_UPLOAD_UID=0859f8d62389ad10bdaf599d6b5840d8
VITE_UPLOAD_TOKEN=b623ba4c187c69f60f3fe3ac0a4e665e

# File Size Limits (in MB)
VITE_UPLOAD_AVATAR_SIZE_LIMIT=5
VITE_UPLOAD_DOC_SIZE_LIMIT=10
VITE_UPLOAD_IMAGE_SIZE_LIMIT=8

# Upload URLs
VITE_UPLOAD_AVATAR_URL=https://www.imgurl.org/api/v2/upload
VITE_UPLOAD_DOC_URL=https://www.imgurl.org/api/v2/upload
VITE_UPLOAD_IMAGE_URL=https://www.imgurl.org/api/v2/upload
```

Here are some helper functions that export the upload config:

```ts
export const uploadConfig: UploadConfig = {
  auth: {
    uid: import.meta.env.VITE_UPLOAD_UID || '',
    token: import.meta.env.VITE_UPLOAD_TOKEN || ''
  },
}

// Helper functions
export const getUploadData = () => ({
  uid: uploadConfig.auth.uid,
  token: uploadConfig.auth.token
}
```
