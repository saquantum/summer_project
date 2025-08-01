package uk.ac.bristol.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.FilterDTO;

@Component
@Aspect
public class PostSearchPreCheckAdvice {

    @Before("@annotation(uk.ac.bristol.advice.PostSearchEndpoint)")
    public void postSearchPreChecks(JoinPoint jp) {
        FilterDTO filter = null;
        Object[] args = jp.getArgs();
        for (Object arg : args) {
            if (arg instanceof FilterDTO) {
                filter = (FilterDTO) arg;
                break;
            }
        }
        if (filter == null) {
            throw new SpExceptions.SystemException("PostSearchEndpoint method does not receive a valid FilterDTO");
        }

        // has limit or offset value without order list
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        // has more pages than upper bound but row id not received
        if (filter.hasLimit() && filter.hasOffset() && filter.getOffset() / filter.getLimit() + 1 > Code.PAGINATION_MAX_PAGE && !filter.hasLastRowId()) {
            throw new SpExceptions.BadRequestException("Deep page query does not receive last row id from last query");
        }
    }

}
