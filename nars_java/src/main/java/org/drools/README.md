This contains a subset of JBoss Rules aka Drools Core (drools-core) version 3.06.

It was the last version of Drools to include a LEAPS rule engine
which can perform better than RETE.

This package involves no external dependencies.

XML, Query, and Query's Bytecode Generation features have been removed.

__We have updated it in several ways for usability and possible performance.__

---------------

## 1.5. Leaps Algorithm
Leaps algorithm for production systems uses a "lazy" approach to condition evaluations. A modified version of this algorithm, implemented as part of Drools v3, attempts to take the best features of both Leaps and Rete approaches in processing facts in the working memory. Leaps is not being described here in detail but only some relevant sections to described how current implementation deviates from "classical" leaps algorithm.
The "classical" Leaps approach puts all incoming (asserted) facts on the main stack according to the order facts were asserted in the working memory (FIFO). It inspects facts one by one trying to find a match for each relevant (based on type of the fact and data types required by CEs) rule by iterating over collections of facts that match datatype of each CE. As soon as such match is found system remembers iteration position to resume such iteration later and fires the rule consequence. After execution of consequence is completed, systems is trying to resume processing by inspecting a fact at the top of the main processing stack and either start processing from beginning or resumes the processing if it was stopped due to finding a match for a rule and consequence firing.

##1.5.1. Conflict resolution
Please note that Leaps allows for rule firing before ALL cross-fact matches were attempted as per RETE approach that explains reported significant performance gains for this algorithm. It's made possible by pushing conflict resolution upfront (sort order of facts on the stack and rules matching order), before matching begins while with RETE all matches should be attempted, activations sorted based on the conflict resolution strategy and head element's consequence fired.
The current implementation allows for flexible conflict resolution strategy selection. Even that it's not exposed as a pluggable feature one can either use supplied conflict resolution strategies (org.drools.leaps.conflict) or develop new ones to modify default behavior. Up to date the general approach was to state conflict resolution stategy without specifying if order activation is based on fact or rules attributes. Current implementation allows for specifying separate ordering based on fact attribute and rule attribute conflict resolution strategy that makes it more apparent.

## 1.5.2. 'not' and 'exists'
The main deviation from the "classical" leaps in this implementation lays in the way it deals with "negative" and "exists" CE processing. "classical" approach makes use of "shadow" fact stack, full scan of relevant collections to determine presence of certain facts matching NOT or EXISTS conditions, and conversion of source rules to account for the instances where retracted facts "release" rule activations that were previously blocked by it. Current implementation takes a different approach. Its functionality is similar to the way RETE negative node by creating tuples with deferred activation in "lazy" manner.
After finding match for all "positive" conditions, current implementation starts looking for facts that might satisfy "not" and "exists" conditions. As soon as such fact found the system stops looking for a given CE and stores found fact handle. When all "not" and "exists" CEs are checked the tuple is being evaluated if it eligible for activation - no matching "not" facts and all "exists" CEs have matching facts. Tuple is either activated or if it has blocking conditions ("not" facts or missing "exists") than it's being deferred for further inspection.
All fact assertions are being used to see if it can activate tuples with deferred activation from above. At the same time all fact that are being retracted inspected to see they remove blocking condition from the deferred tuples or deactivate activation triggered by matching given fact in "exists" condition.