package io.hypersistence.utils.hibernate.id;

import io.hypersistence.tsid.TSID;
import io.hypersistence.utils.common.ReflectionUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.function.Supplier;

/**
 * @author hunknownn
 */
public class TsidFieldGenerator implements AnnotationBasedGenerator, BeforeExecutionGenerator {
    private TSID.Factory factory;

    private TsidGenerator.AttributeType idType;

    public TsidFieldGenerator() {
    }

    @Override
    public void initialize(Annotation annotation, Member member, GeneratorCreationContext generatorCreationContext) {
        idType = TsidGenerator.AttributeType.valueOf(ReflectionUtils.getMemberType(member));
        if (annotation instanceof Tsid) {
            Tsid tsidAnno = (Tsid) annotation;
            Class<? extends Supplier<TSID.Factory>> supplierClass = tsidAnno.value();

            if (supplierClass.equals(Tsid.FactorySupplier.class)) {
                this.factory = Tsid.FactorySupplier.INSTANCE.get();
            } else {
                Supplier<TSID.Factory> supplier = ReflectionUtils.newInstance(supplierClass);
                this.factory = supplier.get();
            }
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "The TsidFieldGenerator can only be used with the @%s annotation. Found: @%s on member %s",
                            Tsid.class.getSimpleName(),
                            annotation.annotationType().getSimpleName(),
                            member.getName()
                    )
            );
        }
    }

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o, Object o1, EventType eventType) {
        return idType.cast(factory.generate());
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
