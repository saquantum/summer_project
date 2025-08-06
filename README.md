# Summer project front-end

This is the front-end of the summer project. You  can run the project through the following steps.

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

You can build front-end alone in the root folder with

```sh
docker build -t myapp .
```

In order to run the whole project,you need to start the backend server at first. Further information is in the main branch.

This web application relys on the following dependencies:

Element-plus: This is the core ui library, most of the components are designed based on it.

Tiptap: A rich text editor. This plays the role as editing message templates for messages that will be sent to the users.

leaflet: This is for the geographic component in the project.

The project structure is as follows:



\item \texttt{/api}: Contains abstracted API communication layers for backend integration.
330
\item \texttt{/assets}: Houses static resources including images, stylesheets, and configuration
files.
331
332
\item \texttt{/components}: Implements reusable UI components following the single
responsibility principle.
333
\item \texttt{/composables}: Contains reactive composition functions for shared business logic.
334
\item \texttt{/router}: Defines application routing logic and navigation guards.
336
\item \texttt{/stores}: Implements centralized state management using Pinia.
337
\item \texttt{/types}: Provides Typescript interface definitions and type declarations.
338
339
\item \texttt{/views}: Contains page-level components corresponding to application routes.
litem \texttt{/utils}: Houses utility functions and helper methods for cross-cutting concerns.
\item \texttt{/tests}: Contains unit test specifications.
\item \texttt{/cypress}: Houses end-to-end testing suites.

The image upload function is implemented in the util folder. It use env file as configuration. You can customized the image server easily by modifing the variable.