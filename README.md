# c-gradle-code

Cooky tool to use Gradle for CMake / Visual C++ generation.

## Status

- use a gradle-like project structure to generate CMake projects which
	- ... I wanted Gradle plugins for my C/++ projects
- **tin;** deflate (binary) files into C/++ headers and add them to the source paths
	- ... in essence; my-own bin2obj but without the .obj
- **smol;** generate shims that make old-C interfaces look more modern
	- ... which I use to wrap OpenGL32 into something sexy
- consume "ported" CMake projects
	- ... I write a CMakeLists.txt that embeds GLFW into my build
- build standalone exe
- handle local project depedencies

## Todo

- use it on other platforms (LInux / Mac)
- perform unit testing
- publish sourceballs to maven and consume maven sourceballs as dependencies
- consume ported projects from GitHub
- option to;
	- build some C/++ project/tool
	- run it in another project
	- use the outputs as compression inputs
- support CodeBlocks (or something equally as portable)

## Setup

Install;

* JDK6+
	- I'm using ... 8 I think
- CMake
	- I think that I'm using a 3.7.2 beta
