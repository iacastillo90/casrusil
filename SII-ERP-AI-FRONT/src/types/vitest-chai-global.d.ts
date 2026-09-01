// El tipo `ExpectStatic` de vitest (`@vitest/expect`) extiende el namespace
// global `Chai`. Ese namespace global lo aporta el archivo empaquetado
// `chai.d.cts` de `@vitest/expect`, pero bajo `moduleResolution: bundler`
// no siempre se carga de forma transitiva, lo que hace que `expect(...)`
// pierda sus firmas de llamada ("ExpectStatic has no call signatures").
//
// Esta referencia explícita fuerza la carga del namespace global `Chai`
// para que `expect` sea invocable en los tests.
/// <reference path="../../node_modules/@vitest/expect/dist/chai.d.cts" />

export {};
