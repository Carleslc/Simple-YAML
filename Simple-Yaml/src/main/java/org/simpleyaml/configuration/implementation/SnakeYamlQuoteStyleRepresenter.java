package org.simpleyaml.configuration.implementation;

import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;

import java.util.Map;
import java.util.Set;

public class SnakeYamlQuoteStyleRepresenter extends SnakeYamlRepresenter {

    public SnakeYamlQuoteStyleRepresenter(final Set<Map.Entry<Class<?>, QuoteStyle>> quoteStyles) {
        super();
        quoteStyles.forEach(this::addRepresenter);
    }

    private void addRepresenter(final Map.Entry<Class<?>, QuoteStyle> e) {
        final Class<?> clazz = e.getKey();
        final Represent baseRepresent = super.findBaseRepresenter(clazz);
        if (baseRepresent != null) {
            final QuoteStyle quoteStyle = e.getValue();
            final Represent representer = new RepresentTypeQuoteStyle(SnakeYamlQuoteValue.getQuoteScalarStyle(quoteStyle), baseRepresent);
            this.representers.put(clazz, representer);
            this.multiRepresenters.put(clazz, representer);
        }
    }

    final class RepresentTypeQuoteStyle implements Represent {

        private final DumperOptions.ScalarStyle scalarStyle;

        private final Represent baseRepresent;

        private RepresentTypeQuoteStyle(final DumperOptions.ScalarStyle scalarStyle, final Represent baseRepresent) {
            this.scalarStyle = scalarStyle;
            this.baseRepresent = baseRepresent;
        }

        @Override
        public Node representData(final Object data) {
            DumperOptions.ScalarStyle defaultScalarStyle = getDefaultScalarStyle();
            setDefaultScalarStyle(this.scalarStyle); // change default scalar style

            final Node representNode = this.representBaseData(data);

            setDefaultScalarStyle(defaultScalarStyle); // restore default scalar style

            return representNode;
        }

        public Node representBaseData(final Object data) {
            return this.baseRepresent.representData(data);
        }

    }

}
