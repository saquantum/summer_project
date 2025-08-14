# Summer Project Front-end

This is the front-end of the summer project. You can run the project by following these steps:

## Project Setup

```sh
pnpm install
```

### Compile and Hot-Reload for Development

```sh
pnpm dev
```

### Compile and Minify for Production

```sh
pnpm build
```

### Docker

You can build the front-end alone in the root folder with:

```sh
docker build -t myapp .
```

To run the whole project, you need to start the backend server first. Further information is available in the main branch.

## Dependencies

This web application relies on the following core dependencies:

- **Element Plus**: The core UI library. Most components are designed based on it.
- **Tiptap**: A rich text editor used for editing message templates that will be sent to users.
- **Leaflet**: Used for geographic components in the project.

## Project Structure

The project structure is as follows:

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

## Image Upload

The image upload function is implemented in the utils folder. It uses an environment file for configuration.

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
})
```

In the development environment, we use a free image server. You can change it to your own server configuration.

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