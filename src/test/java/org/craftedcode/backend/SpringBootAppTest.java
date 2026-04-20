package org.craftedcode.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

class SpringBootAppTest {
    @Test
    void main() {
        assertThatNoException().isThrownBy(() -> SpringBootApp.main(new String[] {}));
    }
}