# Summer Project Front-end

This is the front-end of the summer project, detail information is in the [main branch](https://github.com/saquantum/summer_project).

## Development Setup

As this is the front-end of the project, this part will not cover how to start the entire project, click this [link](https://github.com/saquantum/summer_project) if you do want to. This guide will help you set up a complete development environment for this project

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

There is a repo with front-end alone as well. [link](https://github.com/redial17/summer_project_frontend)

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

#### Full Stack Development

Run both backend and frontend in development mode:

```
# Terminal 1
cd sp
./mvnw clean spring-boot:run

# Terminal 2
cd vue
pnpm dev
```

If the data structure of the database was changed, delete `mock-data-state` folder so that it can reset the database.

### Docker

You can build the front-end alone in the root folder with:

```sh
docker build -t myapp .
```

### Testing

You can run all unit tests with:

```shell
pnpm test
```

---

To run the E2E tests, you need to start the web application first. Further information is available in the [main branch](https://github.com/saquantum/summer_project) . Once the server is ready, you can run all E2E tests. The commands are as follows:

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

## Configuration

### Image upload

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

## User Guide

After installed the application, you should be able to navigate to the log in page via `localhost:port`, by default the port is 80.

The admin and user share the same log in page, under development environment, user and admin's password has been set.

### User interface

Take one of the user as an example

```
username: user_017
password: 123456
```

After login, you will see a asset panel page. Under this page you can have a global view of your assets status.

If you want to see further information, you can click the card's button.

On the left hand side of the page there exist a sidebar, you can edit your personal information, view your inbox to see the messages sent by the admin or the system and the current warnings. You may notice that user can not add a new polygon, this is because the permission is set to false by default. In order to modify the permission, you need to log in to the admin interface.

### Admin interface

The default username and password for admin are:

```
username: admin
password: admin
```

Like user, you can see all the admin pages on the sidebar. The default page for admin is the dashboard, in it you can see all the importance information of the system. The admin interface has the following functionality:

1. manage assets through tables through tables and forms
2. manage users through tables and forms
3. manage message template through a rich text editor

