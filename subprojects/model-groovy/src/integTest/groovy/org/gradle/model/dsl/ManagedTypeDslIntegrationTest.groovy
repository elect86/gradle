/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.dsl

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class ManagedTypeDslIntegrationTest extends AbstractIntegrationSpec {
    def "can configure a child of a managed type using a nested closure syntax"() {
        buildFile << '''
@Managed interface Person extends Named {
    Address getAddress()
}

@Managed interface Address {
    String getCity()
    void setCity(String s)
}

model {
    tasks {
        show(Task) {
            doLast {
                def person = $.barry
                println "$person.name lives in $person.address.city"
            }
        }
    }
    barry(Person) {
        address.city = 'unknown'
    }
    barry {
        address {
            city = 'Melbourne'
        }
    }
}
'''

        when:
        run "show"

        then:
        output.contains("barry lives in Melbourne")
    }

    def "cannot configure a reference property using nested closure"() {
        buildFile << '''
@Managed interface Person extends Named {
    Address getAddress()
    void setAddress(Address a)
}

@Managed interface Address {
    String getCity()
    void setCity(String s)
}

model {
    barry(Person)
    barry {
        address {
            city = 'Melbourne'
        }
    }
}
'''

        when:
        fails "model"

        then:
        failure.assertHasLineNumber(15)
        failure.assertHasCause('Exception thrown while executing model rule: barry { ... } @ build.gradle line 14, column 5')
        failure.assertHasCause('Could not find method address() for arguments [')
    }

    def "cannot configure a scalar list property using nested closure"() {
        buildFile << '''
@Managed interface Person {
    List<String> getNames()
}

model {
    barry(Person)
    barry {
        names {
            add 'barry'
            add 'baz'
        }
    }
}
'''

        when:
        fails "model"

        then:
        failure.assertHasLineNumber(9)
        failure.assertHasCause('Exception thrown while executing model rule: barry { ... } @ build.gradle line 8, column 5')
        failure.assertHasCause('Could not find method names() for arguments [')
    }

    def "cannot configure a property with unmanaged type using nested closure"() {
        buildFile << '''
@Managed interface Person {
    @Unmanaged
    InputStream getInput()
    void setInput(InputStream name)
}

model {
    barry(Person)
    barry {
        input {
            println "broken"
        }
    }
}
'''

        when:
        fails "model"

        then:
        failure.assertHasLineNumber(11)
        failure.assertHasCause('Exception thrown while executing model rule: barry { ... } @ build.gradle line 10, column 5')
        failure.assertHasCause('Could not find method input() for arguments [')
    }

    def "cannot configure a scalar property using nested closure"() {
        buildFile << '''
@Managed interface Person {
    String getName()
    void setName(String name)
}

model {
    barry(Person)
    barry {
        name {
            println "broken"
        }
    }
}
'''

        when:
        fails "model"

        then:
        failure.assertHasLineNumber(10)
        failure.assertHasCause('Exception thrown while executing model rule: barry { ... } @ build.gradle line 9, column 5')
        failure.assertHasCause('Could not find method name() for arguments [')
    }
}
