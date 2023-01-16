import Airport from './pages/Airport';
import Ipsum1A from './pages/Ipsum-1-A';
import Ipsum1B from './pages/Ipsum-1-B';
import Flights from './pages/Flights';
import FlightCrew from './pages/FlightCrew'
import LuggageTrack from './pages/LuggageTrack';
import LuggageScan from './pages/LuggageScan';


const baseName = '/';

const routes = () => [
  {
    iconClass: 'fa fa-globe',
    title: 'Airport',
    to: '/',
    component: Airport,
    subItems: [
      {
        iconClass: 'fa fa-envelope-open',
        title: 'Item 1-A',
        to: '/ipsum/item-1-A',
        component: Ipsum1A
      },
      {
        iconClass: 'fa fa-envelope-closed',
        title: 'Item 1-B',
        to: '/ipsum/item-1-B',
        component: Ipsum1B
      }
    ]
  },
  {
    iconClass: 'fa fa-plane',
    title: 'Flights',
    to: '/flight',
    component: Flights
  },
  {
    iconClass: 'fa fa-users',
    title: 'FlightCrew',
    to: '/flightCrew',
    component: FlightCrew
  },
  {
    iconClass: 'fa fa-suitcase',
    title: 'Luggage Tracking',
    to: '/luggageTrack',
    component: LuggageTrack
  },
  {
    iconClass: 'fa fa-qrcode',
    title: 'Luggage Scanning',
    to: '/luggageScan',
    component: LuggageScan
  }
];

export { baseName, routes };
