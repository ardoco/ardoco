/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.util.Set;

import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Supported programming languages (there are no code model extractors for all of them).
 */
@Deterministic
public enum ProgrammingLanguage {
    JAVA(Set.of("java")), SHELL(Set.of("sh", "bash", "zsh", "ksh", "fish", "csh", "tcsh")), PYTHON(Set.of("py", "pyw", "pyi")), CPP(
            Set.of("cpp", "cc", "cxx", "c++", "hpp", "hh", "hxx", "h++")), AIDL(Set.of("aidl")), ASM(Set.of("asm", "s", "S")), AUTOHOTKEY(
            Set.of("ahk", "ah2")), AUTOIT(Set.of("au3")), AWK(Set.of("awk")), BATCH(Set.of("bat", "cmd")), BAZEL(
            Set.of("bzl", "bazel", "BUILD", "WORKSPACE")), C(Set.of("c", "h")), CLOJURE(Set.of("clj", "cljs", "cljc", "edn")), COFFEESCRIPT(
            Set.of("coffee", "litcoffee")), CS(Set.of("cs")), CSS(Set.of("css")), DART(Set.of("dart")), DM(Set.of("dm")), ELIXIR(Set.of("ex", "exs")), ERLANG(
            Set.of("erl", "hrl")), FORTRAN(Set.of("f", "for", "f77", "f90", "f95", "f03", "f08", "f18")), GO(Set.of("go")), GROOVY(
            Set.of("groovy", "gvy", "gy", "gsh")), HASKELL(Set.of("hs", "lhs")), HCL(Set.of("hcl", "tf", "tfvars")), HTML(
            Set.of("html", "htm", "xhtml")), JAVASCRIPT(Set.of("js", "mjs", "cjs", "jsx")), JINJA(Set.of("jinja", "jinja2", "j2")), JULIA(Set.of("jl")), KOTLIN(
            Set.of("kt", "kts")), LISP(Set.of("lisp", "lsp", "cl", "el")), LUA(Set.of("lua")), MAKEFILE(Set.of("mk", "mak")), MATLAB(
            Set.of("m", "mat")), OBJECTIVEC(Set.of("m", "mm", "h")), OCAML(Set.of("ml", "mli")), PASCAL(Set.of("pas", "pp", "inc")), PERL(
            Set.of("pl", "pm", "pod", "t")), PHP(Set.of("php", "php3", "php4", "php5", "php7", "php8", "phtml")), POWERSHELL(
            Set.of("ps1", "psm1", "psd1")), PROLOG(Set.of("pl", "pro", "prolog")), PROTO(Set.of("proto")), R(Set.of("r", "R")), RUBY(
            Set.of("rb", "rake", "gemspec", "ru", "erb")), RUST(Set.of("rs")), SCALA(Set.of("scala", "sc")), SCSS(Set.of("scss")), SMALI(
            Set.of("smali")), SOLIDITY(Set.of("sol")), SQL(Set.of("sql")), SWIFT(Set.of("swift")), TCL(Set.of("tcl", "tk")), TYPESCRIPT(
            Set.of("ts", "tsx", "mts", "cts")), VBA(Set.of("bas", "cls", "frm", "vba")), VERILOG(Set.of("v", "vh", "sv", "svh")), VHDL(
            Set.of("vhd", "vhdl", "vho", "vht", "vhi")), WASM(Set.of("wat", "wasm")), YARA(Set.of("yar", "yara")), ZIG(Set.of("zig"))//
    , OTHER(Set.of()); //other=all programming languages apart from the ones listed

    private final Set<String> extensions;

    ProgrammingLanguage(Set<String> extensions) {
        this.extensions = extensions;
    }

    public ImmutableSortedSet<String> getExtensions() {
        return SortedSets.immutable.ofAll(extensions);
    }
}
