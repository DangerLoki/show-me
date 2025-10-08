package com.meioQuilo.showme;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GpuMonitorTest {

	@BeforeAll
	static void setup() {
		ShowMeNativeLoader.loadNative(); // garante que JNI foi carregado
	}

	@Test
	void testGetName() {
		String name = GpuMonitor.getName();
		assertNotNull(name, "GPU name should not be null");
		assertFalse(name.isEmpty(), "GPU name should not be empty");
		System.out.println("GPU Name: " + name);
	}

}
