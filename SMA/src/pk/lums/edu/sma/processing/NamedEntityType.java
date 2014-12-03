// =================================================================================================
// Copyright 2011 Alan Ritter
// -------------------------------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this work except in compliance with the License.
// You may obtain a copy of the License in the LICENSE file, or at:
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// =================================================================================================

package pk.lums.edu.sma.processing;

public enum NamedEntityType {
	BENTITY("B-ENTITY"),
	IENTITY("I-ENTITY"),
	Bperson("B-person"),
	Iperson("I-person"),
	Bsportsteam("B-sportsteam"),
	Isportsteam("I-sportsteam"),
	Bgeoloc("B-geo-loc"),
	Igeoloc("I-geo-loc"),
	Bmovie("B-movie"),
	Imovie("I-movie"),
	Bcompany("B-company"),
	Icompany("I-company"),
	Btvshow("B-tvshow"),
	Itvshow("I-tvshow"),
	Bproduct("B-product"),
	Iproduct("I-product"),
	Bother("B-other"),
	Iother("I-other"),
	Bfacility("B-facility"),
	Ifacility("I-facility"),
	BNONE("B-NONE"),
	INONE("I-NONE"),
	Bband("B-band"),
	Iband("I-band"),
	O("O");
	
	// More human-readable version
	public final String name;
	private NamedEntityType(String s) {
		this.name = s;
	}	
}
