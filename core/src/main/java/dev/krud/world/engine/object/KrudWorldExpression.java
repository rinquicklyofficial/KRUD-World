/**
 * KRUD World — World Generator
 * Copyright (C) 2026 Krud Studio
 *
 * Based on KrudWorld World Generator:
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 * https://github.com/VolmitSoftware/KrudWorld
 * License: GPL-3.0
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License.
 */

package dev.krud.world.engine.object;

import com.dfsek.paralithic.Expression;
import com.dfsek.paralithic.eval.parser.Parser;
import com.dfsek.paralithic.eval.parser.Scope;
import dev.krud.world.KrudWorld;
import dev.krud.world.core.loader.KrudWorldRegistrant;
import dev.krud.world.engine.data.cache.AtomicCache;
import dev.krud.world.engine.object.KrudWorldExpressionFunction.FunctionContext;
import dev.krud.world.engine.object.annotations.ArrayType;
import dev.krud.world.engine.object.annotations.Desc;
import dev.krud.world.engine.object.annotations.Required;
import dev.krud.world.util.collection.KList;
import dev.krud.world.util.json.JSONObject;
import dev.krud.world.util.math.RNG;
import dev.krud.world.util.plugin.VolmitSender;
import dev.krud.world.util.stream.ProceduralStream;
import dev.krud.world.util.stream.interpolation.Interpolated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("Represents an KrudWorld Expression")
@Data
@EqualsAndHashCode(callSuper = false)
public class KrudWorldExpression extends KrudWorldRegistrant {
    @ArrayType(type = KrudWorldExpressionLoad.class, min = 1)
    @Desc("Variables to use in this expression")
    private KList<KrudWorldExpressionLoad> variables = new KList<>();

    @ArrayType(type = KrudWorldExpressionFunction.class, min = 1)
    @Desc("Functions to use in this expression")
    private KList<KrudWorldExpressionFunction> functions = new KList<>();

    @Required
    @Desc("The expression. Inherited variables are x, y and z. Avoid using those variable names.")
    private String expression;

    private transient AtomicCache<Expression> expressionCache = new AtomicCache<>();
    private transient AtomicCache<ProceduralStream<Double>> streamCache = new AtomicCache<>();

    private Expression expression() {
        return expressionCache.aquire(() -> {
            Scope scope = new Scope(); // Create variable scope. This scope can hold both constants and invocation variables.
            Parser parser = new Parser();

            try {
                for (KrudWorldExpressionLoad i : variables) {
                    scope.addInvocationVariable(i.getName());
                }

                scope.addInvocationVariable("x");
                scope.addInvocationVariable("y");
                scope.addInvocationVariable("z");
            } catch (Throwable e) {
                e.printStackTrace();
                KrudWorld.error("Script Variable load error in " + getLoadFile().getPath());
            }

            for (KrudWorldExpressionFunction f : functions) {
                if (!f.isValid()) continue;
                f.setData(getLoader());
                parser.registerFunction(f.getName(), f);
            }

            try {
                return parser.parse(getExpression(), scope);
            } catch (Throwable e) {
                e.printStackTrace();
                KrudWorld.error("Script load error in " + getLoadFile().getPath());
            }

            return null;
        });
    }

    public ProceduralStream<Double> stream(RNG rng) {
        return streamCache.aquire(() -> ProceduralStream.of((x, z) -> evaluate(rng, x, z),
                (x, y, z) -> evaluate(rng, x, y, z), Interpolated.DOUBLE));
    }

    public double evaluate(RNG rng, double x, double z) {
        double[] g = new double[3 + getVariables().size()];
        int m = 0;
        for (KrudWorldExpressionLoad i : getVariables()) {
            g[m++] = i.getValue(rng, getLoader(), x, z);
        }

        g[m++] = x;
        g[m++] = z;
        g[m] = -1;

        return expression().evaluate(new FunctionContext(rng), g);
    }

    public double evaluate(RNG rng, double x, double y, double z) {
        double[] g = new double[3 + getVariables().size()];
        int m = 0;
        for (KrudWorldExpressionLoad i : getVariables()) {
            g[m++] = i.getValue(rng, getLoader(), x, y, z);
        }

        g[m++] = x;
        g[m++] = y;
        g[m] = z;

        return expression().evaluate(new FunctionContext(rng), g);
    }

    @Override
    public String getFolderName() {
        return "expressions";
    }

    @Override
    public String getTypeName() {
        return "Expression";
    }

    @Override
    public void scanForErrors(JSONObject p, VolmitSender sender) {

    }
}
