/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.oneclickmlm;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("OneClickMlm")

public interface OneClickMlmConfig extends Config
{

	@ConfigItem(
			position = 1,
			keyName = "useSpec",
			name = "Use Special Attack",
			description = "Uses special attack if its 100% before mining."
	)
	default boolean useSpec(){return false;}

	@ConfigItem(
			position = 1,
			keyName = "dropGems",
			name = "Drop Gems",
			description = "Drops gems if enabled "
	)
	default boolean dropGems(){return true;}

	@ConfigItem(
			position = 2,
			keyName = "repairWheel",
			name = "Repair Wheel",
			description = "Repairs wheel if enabled "
	)
	default boolean repairWheel(){return true;}

	@ConfigItem(
			position = 2,
			keyName = "largeSack",
			name = "Use Large Sack",
			description = "Enable if you have the large sack unlocked "
	)
	default boolean largeSack(){return false;}

	@ConfigItem(
			position = 3,
			keyName = "area",
			name = "Area of mine to use",
			description = "Choose which area the plugin will use"
	)
	default OneClickMlmAreas area() { return OneClickMlmAreas.middle; }

}