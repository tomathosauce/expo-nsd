import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

import ExpoNsdModule from './ExpoNsdModule';

import { Service } from './ExpoNsd.types';

export function requestPermissions() {
  return ExpoNsdModule.requestPermissions();
}

export function startDiscovery() {
  return ExpoNsdModule.startDiscovery();
}
export function stopDiscovery() {
  return ExpoNsdModule.stopDiscovery();
}
const emitter = new EventEmitter(ExpoNsdModule ?? NativeModulesProxy.ExpoNsd);

export function configureDiscovery(serviceType: string, listener: (event: Service) => void): Subscription {
  ExpoNsdModule.configureDiscovery(serviceType)
  return emitter.addListener<Service>('onServiceDiscovered', listener);
}

export { Service };
