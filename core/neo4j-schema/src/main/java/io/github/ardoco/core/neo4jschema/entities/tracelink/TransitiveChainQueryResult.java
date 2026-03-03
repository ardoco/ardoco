package io.github.ardoco.core.neo4jschema.entities.tracelink;

import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;

import lombok.AllArgsConstructor;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;

import java.util.Map;


//    @Value("#{target.result.s}")
//    SentenceNode getS();
//
//    @Value("#{target.result.mid}")
//    TraceableNode getMid();
//
//    @Value("#{target.result.end}")
//    TraceableNode getEnd();
//
//
//    @Value("#{target.result.r1}")
//    Map<String, Object> getR1Properties();
//public interface TransitiveChainQueryResult {
//
//    @Value("#{target.sentence}")
//    SentenceNode getS();
//
//    @Value("#{target.architecture}")
//    TraceableNode getMid();
//
//    @Value("#{target.code}")
//    TraceableNode getEnd();
//}

@Getter
@AllArgsConstructor
public class TransitiveChainQueryResult {
    private final SentenceNode sentence;
    private final TraceableNode architecture; // This will be the mid node
    private final TraceableNode code;         // This will be the end node
}
