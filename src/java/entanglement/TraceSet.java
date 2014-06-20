package entanglement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TraceSet<IdType, ValType, TraceType> {

	String getName();

	int size();

	List<IdType> idOrder();

	Set<Set<IdType>> getEntangledPartitions();

	Set<Map<IdType, ValType>> getValues(Set<IdType> partition);

	Set<Map<IdType, ValType>> getTraces();

	TraceType getTrace(Map<IdType, ValType> mapping);

	Set<? extends TraceSet<IdType, ValType, TraceType>> getSupports(
			Set<Set<IdType>> subpartitioning);

}
