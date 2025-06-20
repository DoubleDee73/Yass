/*
 * Yass Reloaded - Karaoke Editor
 * Copyright (C) 2009-2023 Saruta
 * Copyright (C) 2023 DoubleDee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass;

import java.util.Arrays;
import java.util.List;

public enum DefaultAudioFile {
    UNDEFINED,
    AUDIO,
    INSTRUMENTAL,
    VOCALS;

    public static List<String> validDefaultAudioFiles() {
        return Arrays.stream(DefaultAudioFile.values())
                     .filter(it -> it != UNDEFINED)
                     .map(Enum::toString)
                     .toList();
    }
}
