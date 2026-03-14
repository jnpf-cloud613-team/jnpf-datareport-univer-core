package jnpf.ureport.expression.util;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jnpf.ureport.expression.antlr.ReportLexer;
import jnpf.ureport.expression.antlr.ReportListener;
import jnpf.ureport.expression.antlr.ReportParser;
import jnpf.ureport.expression.assertor.*;
import jnpf.ureport.expression.builder.*;
import jnpf.ureport.expression.expr.Expression;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2016年12月24日
 */
@Getter
@Setter
public class ExpressionUtils {
    public static final String EXPR_PREFIX = "${";
    public static final String EXPR_SUFFIX = "}";
    private static ExpressionVisitor exprVisitor;
    private static Map<Op, Assertor> assertorsMap = ImmutableMap.of(
            Op.Equals, new EqualsAssertor(),
            Op.EqualsGreatThen, new EqualsGreatThenAssertor(),
            Op.EqualsLessThen, new EqualsLessThenAssertor(),
            Op.GreatThen, new GreatThenAssertor(),
            Op.LessThen, new LessThenAssertor(),
            Op.NotEquals, new NotEqualsAssertor(),
            Op.In, new InAssertor(),
            Op.NotIn, new NotInAssertor(),
            Op.Like, new LikeAssertor()
    );
    private static List<ExpressionBuilder> expressionBuilders = ImmutableList.of(
            new StringExpressionBuilder(),
            new VariableExpressionBuilder(),
            new BooleanExpressionBuilder(),
            new IntegerExpressionBuilder(),
            new FunctionExpressionBuilder(),
            new NumberExpressionBuilder(),
            new CellPositionExpressionBuilder(),
            new RelativeCellExpressionBuilder(),
            new SetExpressionBuilder(),
            new CellObjectExpressionBuilder(),
            new NullExpressionBuilder(),
            new CurrentCellValueExpressionBuilder(),
            new CurrentCellDataExpressionBuilder()
    );

    public static boolean conditionEval(Op op, Object left, Object right) {
        Assertor assertor = assertorsMap.get(op);
        boolean result = assertor.eval(left, right);
        return result;
    }

    public static Expression parseExpression(String text) {
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(text);
        ReportLexer lexer = new ReportLexer(antlrInputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParser parser = new ReportParser(tokenStream);
        ReportListener errorListener = new ReportListener();
        parser.addErrorListener(errorListener);
        exprVisitor = new ExpressionVisitor(expressionBuilders);
        Expression expression = exprVisitor.visitEntry(parser.entry());
        String error = errorListener.getErrorMessage();
        return error == null ? expression : null;
    }

    public static ExpressionVisitor getExprVisitor() {
        return exprVisitor;
    }

}
