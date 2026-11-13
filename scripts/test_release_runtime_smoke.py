"""Pure-Python regression checks for the release smoke accessibility parser."""
import unittest

from release_runtime_smoke import find_label, parse_visible_nodes


class ReleaseRuntimeSmokeParserTest(unittest.TestCase):
    def test_accessible_description_and_text_become_tap_points(self):
        xml = """<hierarchy><node content-desc="Nova igra, Odaberi težinu"
            text="" bounds="[20,40][220,140]"/>
            <node content-desc="" text="Započni igru"
            bounds="[100,200][400,300]"/></hierarchy>"""
        nodes = parse_visible_nodes(xml)
        self.assertEqual((120, 90), find_label(nodes, "nova IGRA,")[1])
        self.assertEqual((250, 250), find_label(nodes, "započni igru")[1])

    def test_ignores_invalid_and_nonvisible_bounds(self):
        xml = """<hierarchy><node content-desc="Not visible"
            bounds="[10,10][10,12]"/>
            <node text="No coordinates" bounds="wrong"/>
            <node text="Unesi broj 1" bounds="[1,2][55,88]"/></hierarchy>"""
        nodes = parse_visible_nodes(xml)
        self.assertEqual(1, len(nodes))
        self.assertIsNone(find_label(nodes, "Not visible"))
        self.assertEqual((28, 45), find_label(nodes, "UNESI BROJ 1")[1])

    def test_pause_button_is_not_the_explanatory_sentence(self):
        # Real emulator XML contains both a sentence and the button.
        # The automation must require the visible play glyph, or it would
        # tap the explanatory text and keep the game paused.
        xml = """<hierarchy>
        <node text="Vrijeme je zaustavljeno. Nastavi kada budeš spreman."
              bounds="[20,200][400,270]"/>
        <node text="▶ Nastavi" bounds="[220,300][400,360]"/>
        </hierarchy>"""
        nodes = parse_visible_nodes(xml)
        self.assertEqual((310, 330), find_label(nodes, "▶ Nastavi")[1])

    def test_absent_semantics_fail_closed(self):
        self.assertIsNone(find_label(parse_visible_nodes(
            '<hierarchy><node bounds="[1,2][10,20]"/></hierarchy>'
        ), "Započni igru"))


if __name__ == "__main__":
    unittest.main()
