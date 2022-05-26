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
package com.example.oneclickbankstander;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("OneClickBankstander")

public interface OneClickBankstanderConfig extends Config
{

	@ConfigItem(
			position = 0,
			keyName = "mode",
			name = "Method",
			description = "Choose"
	)
	default OneClickBankstanderModes mode() { return OneClickBankstanderModes.itemOnItem; }


	@ConfigItem(
			position = 1,
			keyName = "itemId1",
			name = "Item ID 1",
			description = "Input item ID"
	)
	default int itemId1()
	{
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "itemId2",
			name = "Item ID 2",
			description = "Input item ID"
	)
	default int itemId2()
	{
		return 0;
	}

	@ConfigItem(
			position = 3,
			keyName = "bankID",
			name = "Bank ID",
			description = "Input bank ID,"
	)
	default int bankID()
	{
		return 0;
	}

	@ConfigItem(
			position = 4,
			keyName = "bankType",
			name = "Bank Type",
			description = "Choose"
	)
	default OneClickBankstanderBankTypes bankType() { return OneClickBankstanderBankTypes.Booth; }
}