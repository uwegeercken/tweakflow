/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Twineworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.twineworks.tweakflow.lang;

import com.twineworks.tweakflow.lang.analysis.Analysis;
import com.twineworks.tweakflow.lang.analysis.AnalysisResult;
import com.twineworks.tweakflow.lang.analysis.constants.ConstantOpsFoldingVisitor;
import com.twineworks.tweakflow.lang.analysis.ops.OpBuilderVisitor;
import com.twineworks.tweakflow.lang.analysis.ops.OpSpecializationVisitor;
import com.twineworks.tweakflow.lang.analysis.references.ClosureAnalysisVisitor;
import com.twineworks.tweakflow.lang.analysis.references.DependencyVerification;
import com.twineworks.tweakflow.lang.analysis.references.MetaDataAnalysisVisitor;
import com.twineworks.tweakflow.lang.analysis.scope.ExpressionResolverVisitor;
import com.twineworks.tweakflow.lang.analysis.scope.ScopeBuilderVisitor;
import com.twineworks.tweakflow.lang.ast.expressions.ExpressionNode;
import com.twineworks.tweakflow.lang.interpreter.DebugHandler;
import com.twineworks.tweakflow.lang.interpreter.DefaultDebugHandler;
import com.twineworks.tweakflow.lang.interpreter.Evaluator;
import com.twineworks.tweakflow.lang.interpreter.RuntimeSet;
import com.twineworks.tweakflow.lang.load.loadpath.LoadPath;
import com.twineworks.tweakflow.lang.load.loadpath.MemoryLocation;
import com.twineworks.tweakflow.lang.parse.ParseResult;
import com.twineworks.tweakflow.lang.parse.Parser;
import com.twineworks.tweakflow.lang.parse.units.ParseUnit;
import com.twineworks.tweakflow.lang.runtime.Runtime;
import com.twineworks.tweakflow.lang.scope.GlobalScope;
import com.twineworks.tweakflow.lang.values.Value;

import java.util.Collections;
import java.util.List;

public class TweakFlow {

  public static Runtime compile(LoadPath loadPath, String path){
    return compile(loadPath, path, new DefaultDebugHandler());
  }


  public static Runtime compile(LoadPath loadPath, String path, DebugHandler debugHandler){
    return compile(loadPath, Collections.singletonList(path), debugHandler);
  }

  public static Runtime compile(LoadPath loadPath, List<String> paths){
    return compile(loadPath, paths, new DefaultDebugHandler());
  }

  public static Runtime compile(LoadPath loadPath, List<String> paths, DebugHandler debugHandler){
    AnalysisResult analysisResult = Analysis.analyze(paths, loadPath);
    if (analysisResult.isError()) throw analysisResult.getException();
    RuntimeSet runtimeSet = new RuntimeSet(analysisResult);
    return new Runtime(runtimeSet, debugHandler);
  }

  public static Value evaluateExpression(String exp){
    return evaluateExpression(exp, true);
  }

  public static Value evaluateExpression(String exp, boolean allowNativeFunctions){

    ParseUnit parseUnit = new MemoryLocation.Builder()
        .allowNativeFunctions(allowNativeFunctions)
        .add("<eval>", exp)
        .build()
        .getParseUnit("<eval>");

    ParseResult parseResult = new Parser(parseUnit).parseExpression();

    if (parseResult.isError()){
      throw parseResult.getException();
    }

    ExpressionNode node = (ExpressionNode) parseResult.getNode();

    new MetaDataAnalysisVisitor().visit(node);
    new ScopeBuilderVisitor(new GlobalScope()).visit(node);
    new ExpressionResolverVisitor().visit(node);
    new ClosureAnalysisVisitor().visit(node);
    DependencyVerification.verify(node);
    new OpBuilderVisitor().visit(node);
    new ConstantOpsFoldingVisitor().visit(node);
    new OpSpecializationVisitor().visit(node);

    return Evaluator.evaluateInEmptyScope(node);

  }

}
