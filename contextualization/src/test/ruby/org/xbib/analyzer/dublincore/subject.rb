require 'java'
java_import 'org.xbib.elements.Element'
java_import 'org.xbib.elements.ElementBuilder'
class Subject
      java_implements 'org.xbib.elements.Element'
      java_signature 'Element build(ElementBuilder, Object, Object)'
      def build(a, b, c)
        puts "Hi, there!"
      end
end
$subjectElement = Subject.new
